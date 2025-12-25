package imbuy.lot.application.service;

import com.imbuy.events.TopicNames;
import com.imbuy.events.lot.LotCreatedEvent;
import imbuy.lot.application.dto.*;
import imbuy.lot.application.mapper.LotMapper;
import imbuy.lot.application.port.in.LotUseCase;
import imbuy.lot.application.port.out.BidPort;
import imbuy.lot.application.port.out.LotRepositoryPort;
import imbuy.lot.application.port.out.UserPort;
import imbuy.lot.domain.model.Lot;
import imbuy.lot.domain.service.LotDomainService;
import imbuy.lot.domain.enums.LotStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class LotServiceImpl implements LotUseCase {

    private final LotRepositoryPort lotRepository;
    private final UserPort userPort;
    private final BidPort bidPort;
    private final LotDomainService domainService;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    public LotDto getLotById(Long id) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));
        return LotMapper.toDto(lot);
    }

    @Override
    public LotDto createLot(CreateLotDto dto, Long userId) {
        userPort.getUserById(userId);

        domainService.validateBidStep(dto.bid_step());
        domainService.validateEndDate(dto.end_date());

        Lot lot = Lot.builder()
                .title(dto.title())
                .description(dto.description())
                .startPrice(dto.start_price())
                .currentPrice(dto.start_price())
                .bidStep(dto.bid_step())
                .ownerId(userId)
                .categoryId(dto.category_id())
                .status(LotStatus.PENDING_APPROVAL)
                .startDate(dto.start_date())
                .endDate(dto.end_date())
                .createdAt(LocalDateTime.now())
                .build();

        LotCreatedEvent event = new LotCreatedEvent(
                "lot-service",
                lot.getId(),
                lot.getTitle(),
                lot.getOwnerId(),
                lot.getStartPrice(),
                lot.getEndDate()
        );

        kafkaTemplate.send(TopicNames.LOT_EVENTS, event);

        return LotMapper.toDto(lotRepository.save(lot));
    }

    @Override
    public LotDto approveLot(Long id, Long userId) {

        UserDto user = userPort.getUserById(userId);
        if (user == null ||
                (!"SUPERVISOR".equalsIgnoreCase(user.role())
                        && !"MODERATOR".equalsIgnoreCase(user.role()))) {
            throw new IllegalStateException("User has no permission to approve lot");
        }

        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found"));
        if (lot.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner cannot approve their own lot");
        }

        Lot approved = domainService.approve(lot);
        return LotMapper.toDto(lotRepository.save(approved));
    }


    @Override
    public LotDto cancelLot(Long id, Long userId, String reason) {

        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found"));

        UserDto user = userPort.getUserById(userId);

        boolean isOwner = lot.getOwnerId().equals(userId);
        boolean isModerator = user != null && "MODERATOR".equalsIgnoreCase(user.role());

        if (!isOwner && !isModerator) {
            throw new IllegalStateException("No permission to cancel lot");
        }

        Lot cancelled = domainService.cancel(lot);
        return LotMapper.toDto(lotRepository.save(cancelled));
    }


    @Override
    public LotDto updateLot(Long id, UpdateLotDto dto, Long userId) {

        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found"));

        if (!lot.getOwnerId().equals(userId)) {
            throw new IllegalStateException("Only owner can update lot");
        }

        domainService.validateBidStep(
                dto.bid_step() != null ? dto.bid_step() : lot.getBidStep()
        );

        Lot updated = lot.toBuilder()
                .title(dto.title() != null ? dto.title() : lot.getTitle())
                .description(dto.description() != null ? dto.description() : lot.getDescription())
                .bidStep(dto.bid_step() != null ? dto.bid_step() : lot.getBidStep())
                .endDate(dto.end_date() != null ? dto.end_date() : lot.getEndDate())
                .categoryId(dto.category_id() != null ? dto.category_id() : lot.getCategoryId())
                .build();

        return LotMapper.toDto(lotRepository.save(updated));
    }


    @Override
    public void deleteLot(Long id, Long userId) {

        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found"));

        if (!lot.getOwnerId().equals(userId)) {
            throw new IllegalStateException("Only owner can delete lot");
        }

        if (lot.getStatus() == LotStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete active lot");
        }

        lotRepository.delete(lot);
    }

}
