package imbuy.bid.application.mapper;

import imbuy.bid.domain.model.Bid;
import imbuy.bid.application.dto.BidDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BidMapper {

    @Mapping(target = "bidder_username", source = "bidderId", qualifiedByName = "generateName")
    @Mapping(target = "bidder_id", source = "bidderId")
    BidDto mapToDto(Bid bid);

    @Named("generateName")
    default String generateName(Long bidderId) {
        return "User " + bidderId;
    }
}