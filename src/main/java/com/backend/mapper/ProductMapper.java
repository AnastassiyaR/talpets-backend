package com.backend.mapper;


import com.backend.dto.ProductResponseDTO;
import com.backend.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
    ProductResponseDTO toFilterDto(Product product);
    List<ProductResponseDTO> toFilterDtoList(List<Product> products);
}
