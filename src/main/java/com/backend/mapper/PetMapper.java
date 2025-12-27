package com.backend.mapper;


import com.backend.dto.PetDTO;
import com.backend.model.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetMapper {

    PetDTO toDto(Pet pet);

    /**
     * Converts PetDTO to Pet entity for creation.
     * Ignores id and user fields as they should be set separately.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Pet toEntity(PetDTO petDto);

    /**
     * Updates an existing Pet entity from PetDTO.
     * Ignores id and user to prevent overwriting these fields.
     * Used for update operations where pet already exists.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(PetDTO petDto, @MappingTarget Pet pet);
}
