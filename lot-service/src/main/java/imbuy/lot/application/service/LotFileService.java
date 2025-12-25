package imbuy.lot.application.service;

import imbuy.lot.application.port.out.LotFileRepositoryPort;
import imbuy.lot.application.port.out.LotRepositoryPort;
import imbuy.lot.domain.model.LotFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LotFileService {

    private final LotFileRepositoryPort lotFileRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;

    public void saveFileForLot(Long lotId, Long fileId, String fileName, 
                               String filePath, String contentType, Long fileSize) {
        log.info("Saving file {} for lot {}", fileId, lotId);

        // Проверяем, что лот существует
        lotRepositoryPort.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found with id: " + lotId));

        // Проверяем, что файл еще не связан с этим лотом
        if (lotFileRepositoryPort.existsByLotIdAndFileId(lotId, fileId)) {
            log.warn("File {} already exists for lot {}", fileId, lotId);
            return;
        }

        // Сохраняем файл для лота
        LotFile lotFile = LotFile.builder()
                .lotId(lotId)
                .fileId(fileId)
                .fileName(fileName)
                .filePath(filePath)
                .contentType(contentType)
                .fileSize(fileSize)
                .uploadedAt(LocalDateTime.now())
                .build();

        lotFileRepositoryPort.save(lotFile);
        log.info("File {} successfully saved for lot {}", fileId, lotId);
    }
}

