package com.imbuy.file.application.mapper;

import com.imbuy.file.application.dto.FileDto;
import com.imbuy.file.domain.model.FileMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileMapper {
    @Mapping(target = "downloadUrl", expression = "java(\"/api/files/\" + fileMetadata.getId() + \"/download\")")
    FileDto toDto(FileMetadata fileMetadata);
}

