package com.imbuy.file.application.port.in;

import com.imbuy.file.application.dto.FileDto;
import org.springframework.core.io.Resource;

import java.util.List;

public interface GetFileUseCase {
    FileDto getFileMetadata(Long fileId);
    Resource getFileResource(Long fileId);
//    List<FileDto> getFilesByUser(Long userId);
}

