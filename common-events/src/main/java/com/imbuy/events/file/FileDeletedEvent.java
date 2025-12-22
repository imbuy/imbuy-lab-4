package com.imbuy.events.file;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileDeletedEvent extends BaseEvent {
    private Long fileId;
    private String fileName;
    private Long deletedBy;

    public FileDeletedEvent(String sourceService, Long fileId, String fileName, Long deletedBy) {
        super(sourceService);
        this.fileId = fileId;
        this.fileName = fileName;
        this.deletedBy = deletedBy;
    }
}

