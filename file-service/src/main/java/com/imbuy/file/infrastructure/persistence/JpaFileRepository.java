package com.imbuy.file.infrastructure.persistence;

import com.imbuy.file.domain.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaFileRepository extends JpaRepository<FileMetadata, Long> {
//    List<FileMetadata> findByUploadedByOrderByUploadedAtDesc(Long uploadedBy);
}

