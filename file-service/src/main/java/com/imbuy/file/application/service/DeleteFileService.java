package com.imbuy.file.application.service;

import com.imbuy.events.TopicNames;
import com.imbuy.events.file.FileDeletedEvent;
import com.imbuy.file.application.port.in.DeleteFileUseCase;
import com.imbuy.file.application.port.out.FilePersistencePort;
import com.imbuy.file.application.port.out.FileStoragePort;
import com.imbuy.file.application.port.out.KafkaEventPort;
import com.imbuy.file.domain.model.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteFileService implements DeleteFileUseCase {

    private final FilePersistencePort filePersistencePort;
    private final FileStoragePort fileStoragePort;
    private final KafkaEventPort kafkaEventPort;

    @Override
    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        log.info("Deleting file {} by user {}", fileId, userId);

        FileMetadata metadata = filePersistencePort.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        // Delete from storage
        fileStoragePort.delete(metadata.getFilePath());

        // Delete metadata
        filePersistencePort.delete(fileId);

        // Publish event
        FileDeletedEvent event = new FileDeletedEvent(
                "file-service",
                fileId,
                metadata.getFileName(),
                userId
        );
        kafkaEventPort.publishEvent(TopicNames.FILE_EVENTS, event);
    }
}

