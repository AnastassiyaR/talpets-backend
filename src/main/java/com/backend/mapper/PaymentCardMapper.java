package com.backend.mapper;


import com.backend.dto.PaymentCardResponseDTO;
import com.backend.model.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentCardMapper {
    PaymentCardResponseDTO toResponseDTO(PaymentCard card);
}
