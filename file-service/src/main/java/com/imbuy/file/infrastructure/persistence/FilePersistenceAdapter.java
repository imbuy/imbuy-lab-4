package com.imbuy.file.infrastructure.persistence;

import com.imbuy.file.application.port.out.FilePersistencePort;
import com.imbuy.file.domain.model.FileMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FilePersistenceAdapter implements FilePersistencePort {

    private final JpaFileRepository repository;

    @Override
    public FileMetadata save(FileMetadata fileMetadata) {
        return repository.save(fileMetadata);
    }

    @Override
    public Optional<FileMetadata> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}

