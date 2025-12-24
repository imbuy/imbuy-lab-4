package com.imbuy.file.application.port.in;

import com.imbuy.file.application.dto.FileDto;
import org.springframework.web.multipart.MultipartFile;

public interface UploadFileUseCase {
    FileDto uploadFile(MultipartFile file, Long lotId);
}

