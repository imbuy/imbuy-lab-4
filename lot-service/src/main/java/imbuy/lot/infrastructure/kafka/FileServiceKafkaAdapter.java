package imbuy.lot.infrastructure.kafka;

import com.imbuy.events.TopicNames;
import com.imbuy.events.file.FileUploadedEvent;
import imbuy.lot.application.service.LotFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileServiceKafkaAdapter {

    private final LotFileService lotFileService;

    @KafkaListener(topics = TopicNames.FILE_EVENTS, groupId = "lot-service")
    public void handleFileUploaded(FileUploadedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received file uploaded event: fileId={}, lotId={}, fileName={}", 
                    event.getFileId(), event.getLotId(), event.getFileName());

            if (event.getLotId() != null) {
                lotFileService.saveFileForLot(
                        event.getLotId(),
                        event.getFileId(),
                        event.getFileName(),
                        event.getFilePath(),
                        event.getContentType(),
                        event.getFileSize()
                );
                log.info("File {} successfully associated with lot {}", event.getFileId(), event.getLotId());
            } else {
                log.warn("File uploaded event received without lotId: fileId={}", event.getFileId());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error handling file uploaded event: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}

