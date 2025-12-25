package imbuy.lot.infrastructure.persistence.mapper;

import imbuy.lot.domain.model.LotFile;
import imbuy.lot.infrastructure.persistence.entity.LotFileEntity;

public final class LotFilePersistenceMapper {

    private LotFilePersistenceMapper() {}

    public static LotFile toDomain(LotFileEntity e) {
        return LotFile.builder()
                .id(e.getId())
                .lotId(e.getLotId())
                .fileId(e.getFileId())
                .fileName(e.getFileName())
                .filePath(e.getFilePath())
                .contentType(e.getContentType())
                .fileSize(e.getFileSize())
                .uploadedAt(e.getUploadedAt())
                .build();
    }

    public static LotFileEntity toEntity(LotFile d) {
        return LotFileEntity.builder()
                .id(d.getId())
                .lotId(d.getLotId())
                .fileId(d.getFileId())
                .fileName(d.getFileName())
                .filePath(d.getFilePath())
                .contentType(d.getContentType())
                .fileSize(d.getFileSize())
                .uploadedAt(d.getUploadedAt())
                .build();
    }
}

