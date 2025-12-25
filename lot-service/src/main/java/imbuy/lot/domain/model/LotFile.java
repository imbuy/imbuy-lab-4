package imbuy.lot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class LotFile {
    private Long id;
    private Long lotId;
    private Long fileId;
    private String fileName;
    private String filePath;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}

