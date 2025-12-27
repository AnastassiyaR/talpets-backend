package com.backend.mapper;


import com.backend.dto.OrderResponseDTO;
import com.backend.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    OrderResponseDTO toResponseDTO(Order order);
}
