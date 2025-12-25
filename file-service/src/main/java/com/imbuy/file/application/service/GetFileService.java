package com.imbuy.file.application.service;

import com.imbuy.file.application.dto.FileDto;
import com.imbuy.file.application.mapper.FileMapper;
import com.imbuy.file.application.port.in.GetFileUseCase;
import com.imbuy.file.application.port.out.FilePersistencePort;
import com.imbuy.file.application.port.out.FileStoragePort;
import com.imbuy.file.domain.model.FileMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFileService implements GetFileUseCase {

    private final FilePersistencePort filePersistencePort;
    private final FileStoragePort fileStoragePort;
    private final FileMapper fileMapper;

    @Override
    public FileDto getFileMetadata(Long fileId) {
        FileMetadata metadata = filePersistencePort.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        return fileMapper.toDto(metadata);
    }

    @Override
    public Resource getFileResource(Long fileId) {
        FileMetadata metadata = filePersistencePort.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        return fileStoragePort.loadAsResource(metadata.getFilePath());
    }

//    @Override
//    public List<FileDto> getFilesByUser(Long userId) {
//        return filePersistencePort.findByUploadedBy(userId).stream()
//                .map(fileMapper::toDto)
//                .collect(Collectors.toList());
//    }
}

