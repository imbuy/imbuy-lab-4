package com.imbuy.file.application.port.out;

import com.imbuy.file.domain.model.FileMetadata;

import java.util.Optional;

public interface FilePersistencePort {
    FileMetadata save(FileMetadata fileMetadata);
    Optional<FileMetadata> findById(Long id);
    void delete(Long id);
}

