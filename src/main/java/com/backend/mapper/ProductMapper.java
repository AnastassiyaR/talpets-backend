package com.backend.mapper;


import com.backend.dto.ProductResponseDTO;
import com.backend.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
    ProductResponseDTO toFilterDto(Product product);

    List<ProductResponseDTO> toFilterDtoList(List<Product> products);

    Product toEntity(ProductResponseDTO dto);

    /**
     * Updates existing Product entity from DTO.
     * Ignores the ID field to prevent overwriting.
     * Used for update operations where product already exists.
     *
     * @param dto the product DTO with updated values
     * @param product the existing product entity to update (modified in place)
     */
    @Mapping(target = "id", ignore = true)
    void updateProductFromDto(ProductResponseDTO dto, @MappingTarget Product product);
}
