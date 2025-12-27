package com.backend.service;


import com.backend.repository.PetRepository;
import com.backend.dto.PetDTO;
import com.backend.mapper.PetMapper;
import com.backend.model.Pet;
import com.backend.model.User;
import com.backend.exception.ResourceNotFoundException;
import com.backend.exception.UnauthorizedException;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetMapper petMapper;

    private static final String USER_NOT_FOUND = "User not found with id: ";
    private static final String PET_NOT_FOUND = "Pet not found with id: ";
    private static final String UNAUTHORIZED_UPDATE = "You don't have permission to update this pet";
    private static final String UNAUTHORIZED_DELETE = "You don't have permission to delete this pet";

    @Transactional(readOnly = true)
    public List<PetDTO> getAllPetsByUserId(Long userId) {
        log.debug("Fetching all pets for userId: {}", userId);
        List<Pet> pets = petRepository.findByUserIdOrderByIdDesc(userId);
        log.debug("Found {} pets for userId: {}", pets.size(), userId);
        return pets.stream()
                .map(petMapper::toDto)
                .toList();
    }

    @Transactional
    public PetDTO createPet(PetDTO petDto, Long userId) {
        log.debug("Creating new pet '{}' for userId: {}", petDto.getName(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ResourceNotFoundException(USER_NOT_FOUND + userId);
                });

        Pet pet = petMapper.toEntity(petDto);
        pet.setUser(user);

        Pet savedPet = petRepository.save(pet);
        log.debug("Pet created successfully with id: {}", savedPet.getId());
        return petMapper.toDto(savedPet);
    }

    @Transactional
    public PetDTO updatePet(Long petId, PetDTO petDto, Long userId) {
        log.debug("Updating pet {} for userId: {}", petId, userId);

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> {
                    log.error("Pet not found: {}", petId);
                    return new ResourceNotFoundException(PET_NOT_FOUND + petId);
                });

        if (!pet.getUser().getId().equals(userId)) {
            log.warn("Unauthorized update attempt: userId {} tried to update pet {} owned by userId {}",
                    userId, petId, pet.getUser().getId());
            throw new UnauthorizedException(UNAUTHORIZED_UPDATE);
        }

        petMapper.updateEntityFromDto(petDto, pet);

        Pet updatedPet = petRepository.save(pet);
        log.debug("Pet {} updated successfully", petId);
        return petMapper.toDto(updatedPet);
    }

    @Transactional
    public void deletePet(Long petId, Long userId) {
        log.debug("Deleting pet {} for userId: {}", petId, userId);

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> {
                    log.error("Pet not found: {}", petId);
                    return new ResourceNotFoundException(PET_NOT_FOUND + petId);
                });

        if (!pet.getUser().getId().equals(userId)) {
            log.warn("Unauthorized delete attempt: userId {} tried to delete pet {} owned by userId {}",
                    userId, petId, pet.getUser().getId());
            throw new UnauthorizedException(UNAUTHORIZED_DELETE);
        }

        petRepository.delete(pet);
        log.debug("Pet {} deleted successfully", petId);
    }
}
