package com.imbuy.file.application.service;

import com.imbuy.events.TopicNames;
import com.imbuy.events.file.FileUploadedEvent;
import com.imbuy.file.application.dto.FileDto;
import com.imbuy.file.application.mapper.FileMapper;
import com.imbuy.file.application.port.in.UploadFileUseCase;
import com.imbuy.file.application.port.out.FilePersistencePort;
import com.imbuy.file.application.port.out.FileStoragePort;
import com.imbuy.file.application.port.out.KafkaEventPort;
import com.imbuy.file.domain.model.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadFileService implements UploadFileUseCase {

    private final FileStoragePort fileStoragePort;
    private final FilePersistencePort filePersistencePort;
    private final KafkaEventPort kafkaEventPort;
    private final FileMapper fileMapper;

    @Override
    @Transactional
    public FileDto uploadFile(MultipartFile file, Long lotId) {
        log.info("Uploading file: {} for lot {} by user", file.getOriginalFilename(), lotId);

        String filePath = fileStoragePort.store(file);

        FileMetadata metadata = FileMetadata.builder()
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .lotId(lotId)
                .build();

        FileMetadata saved = filePersistencePort.save(metadata);
        FileDto dto = fileMapper.toDto(saved);

        FileUploadedEvent event = new FileUploadedEvent(
                "file-service",
                saved.getId(),
                saved.getFileName(),
                saved.getFilePath(),
                saved.getFileSize(),
                saved.getContentType(),
                saved.getLotId()
        );
        kafkaEventPort.publishEvent(TopicNames.FILE_EVENTS, event);

        return dto;
    }
}

