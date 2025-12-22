package com.imbuy.events.file;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileUploadedEvent extends BaseEvent {
    private Long fileId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;

    public FileUploadedEvent(String sourceService, Long fileId, String fileName, 
                            String filePath, Long fileSize, String contentType) {
        super(sourceService);
        this.fileId = fileId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }
}

