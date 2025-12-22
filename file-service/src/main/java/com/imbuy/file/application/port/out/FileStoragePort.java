package com.imbuy.file.application.port.out;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStoragePort {
    String store(MultipartFile file);
    Resource loadAsResource(String filePath);
    void delete(String filePath);
    Path getStoragePath();
}

