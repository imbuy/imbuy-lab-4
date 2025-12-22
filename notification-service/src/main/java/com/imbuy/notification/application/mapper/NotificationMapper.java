package com.imbuy.notification.application.mapper;

import com.imbuy.notification.application.dto.NotificationDto;
import com.imbuy.notification.domain.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {
    NotificationDto toDto(Notification notification);
    Notification toDomain(NotificationDto dto);
}

