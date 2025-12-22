package com.imbuy.file.presentation.controller;

import com.imbuy.file.application.dto.FileDto;
import com.imbuy.file.application.port.in.DeleteFileUseCase;
import com.imbuy.file.application.port.in.GetFileUseCase;
import com.imbuy.file.application.port.in.UploadFileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadFileUseCase uploadFileUseCase;
    private final GetFileUseCase getFileUseCase;
    private final DeleteFileUseCase deleteFileUseCase;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file", description = "Загрузка файла")
    public ResponseEntity<FileDto> uploadFile(
            @Parameter(
                    description = "Файл для загрузки",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(uploadFileUseCase.uploadFile(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getFileMetadata(@PathVariable Long id) {
        return ResponseEntity.ok(getFileUseCase.getFileMetadata(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Resource resource = getFileUseCase.getFileResource(id);
        FileDto metadata = getFileUseCase.getFileMetadata(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + metadata.getFileName() + "\"")
                .body(resource);
    }

//    @GetMapping("/users/{userId}")
//    public ResponseEntity<List<FileDto>> getFilesByUser(@PathVariable Long userId) {
//        return ResponseEntity.ok(getFileUseCase.getFilesByUser(userId));
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        deleteFileUseCase.deleteFile(id, userId);
        return ResponseEntity.noContent().build();
    }
}

