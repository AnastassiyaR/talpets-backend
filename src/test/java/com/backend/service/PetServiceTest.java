package com.backend.service;

import com.backend.dto.PetDTO;
import com.backend.exception.ResourceNotFoundException;
import com.backend.exception.UnauthorizedException;
import com.backend.mapper.PetMapper;
import com.backend.model.Pet;
import com.backend.model.User;
import com.backend.repository.PetRepository;
import com.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetMapper petMapper;

    @InjectMocks
    private PetService petService;

    private User user;
    private Pet pet;
    private PetDTO petDTO;
    private Long userId;
    private Long petId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        petId = 10L;

        user = User.builder()
                .id(userId)
                .email("test@mail.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .build();

        pet = Pet.builder()
                .id(petId)
                .user(user)
                .name("Buddy")
                .breed("Golden Retriever")
                .gender("Male")
                .birthday(LocalDate.of(2020, 5, 15))
                .age("3 years old")
                .description("Friendly dog")
                .photo("buddy.jpg")
                .build();

        petDTO = new PetDTO(
                petId,
                "Buddy",
                "Golden Retriever",
                "Male",
                LocalDate.of(2020, 5, 15),
                "3 years old",
                "Friendly dog",
                "buddy.jpg"
        );
    }

    // GET ALL PETS BY USER ID TESTS

    @Test
    void getAllPetsByUserId_shouldReturnListOfPets() {
        // GIVEN
        Pet pet2 = Pet.builder()
                .id(11L)
                .user(user)
                .name("Max")
                .breed("Labrador")
                .gender("Male")
                .birthday(LocalDate.of(2019, 3, 10))
                .age("4 years old")
                .build();

        PetDTO petDTO2 = new PetDTO(
                11L,
                "Max",
                "Labrador",
                "Male",
                LocalDate.of(2019, 3, 10),
                "4 years old",
                null,
                null
        );

        List<Pet> pets = Arrays.asList(pet2, pet); // ordered by id desc
        given(petRepository.findByUserIdOrderByIdDesc(userId))
                .willReturn(pets);
        given(petMapper.toDto(pet2))
                .willReturn(petDTO2);
        given(petMapper.toDto(pet))
                .willReturn(petDTO);

        // WHEN
        List<PetDTO> result = petService.getAllPetsByUserId(userId);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Max", result.get(0).getName());
        assertEquals("Buddy", result.get(1).getName());

        then(petRepository).should().findByUserIdOrderByIdDesc(userId);
        then(petMapper).should(times(2)).toDto(any(Pet.class));
    }

    @Test
    void getAllPetsByUserId_shouldReturnEmptyList_whenUserHasNoPets() {
        // GIVEN
        given(petRepository.findByUserIdOrderByIdDesc(userId))
                .willReturn(Collections.emptyList());

        // WHEN
        List<PetDTO> result = petService.getAllPetsByUserId(userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());

        then(petRepository).should().findByUserIdOrderByIdDesc(userId);
        then(petMapper).should(never()).toDto(any(Pet.class));
    }

    @Test
    void getAllPetsByUserId_shouldReturnSinglePet() {
        // GIVEN
        List<Pet> pets = Collections.singletonList(pet);
        given(petRepository.findByUserIdOrderByIdDesc(userId))
                .willReturn(pets);
        given(petMapper.toDto(pet))
                .willReturn(petDTO);

        // WHEN
        List<PetDTO> result = petService.getAllPetsByUserId(userId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Buddy", result.get(0).getName());

        then(petRepository).should().findByUserIdOrderByIdDesc(userId);
        then(petMapper).should().toDto(pet);
    }

    // CREATE PET TESTS

    @Test
    void createPet_shouldCreatePetSuccessfully() {
        // GIVEN
        PetDTO newPetDTO = new PetDTO(
                null,
                "Luna",
                "Husky",
                "Female",
                LocalDate.of(2021, 8, 20),
                "2 years old",
                "Playful and energetic",
                null
        );

        Pet newPet = Pet.builder()
                .name("Luna")
                .breed("Husky")
                .gender("Female")
                .birthday(LocalDate.of(2021, 8, 20))
                .age("2 years old")
                .description("Playful and energetic")
                .build();

        Pet savedPet = Pet.builder()
                .id(12L)
                .user(user)
                .name("Luna")
                .breed("Husky")
                .gender("Female")
                .birthday(LocalDate.of(2021, 8, 20))
                .age("2 years old")
                .description("Playful and energetic")
                .build();

        PetDTO savedPetDTO = new PetDTO(
                12L,
                "Luna",
                "Husky",
                "Female",
                LocalDate.of(2021, 8, 20),
                "2 years old",
                "Playful and energetic",
                null
        );

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));
        given(petMapper.toEntity(newPetDTO))
                .willReturn(newPet);
        given(petRepository.save(newPet))
                .willReturn(savedPet);
        given(petMapper.toDto(savedPet))
                .willReturn(savedPetDTO);

        // WHEN
        PetDTO result = petService.createPet(newPetDTO, userId);

        // THEN
        assertNotNull(result);
        assertEquals(12L, result.getId());
        assertEquals("Luna", result.getName());
        assertEquals("Husky", result.getBreed());
        assertEquals("Female", result.getGender());

        then(userRepository).should().findById(userId);
        then(petMapper).should().toEntity(newPetDTO);
        then(petRepository).should().save(newPet);
        then(petMapper).should().toDto(savedPet);
    }

    @Test
    void createPet_shouldThrowResourceNotFoundException_whenUserNotFound() {
        // GIVEN
        PetDTO newPetDTO = new PetDTO(
                null,
                "Luna",
                "Husky",
                "Female",
                LocalDate.of(2021, 8, 20),
                "2 years old",
                "Playful",
                null
        );

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> petService.createPet(newPetDTO, userId)
        );

        assertEquals("User not found with id: " + userId, exception.getMessage());

        then(userRepository).should().findById(userId);
        then(petMapper).should(never()).toEntity(any());
        then(petRepository).should(never()).save(any());
    }

    // UPDATE PET TESTS

    @Test
    void updatePet_shouldUpdatePetSuccessfully() {
        // GIVEN
        PetDTO updateDTO = new PetDTO(
                petId,
                "Buddy Updated",
                "Golden Retriever Mix",
                "Male",
                LocalDate.of(2020, 5, 15),
                "3 years old",
                "Very friendly dog",
                "new-buddy.jpg"
        );

        given(petRepository.findById(petId))
                .willReturn(Optional.of(pet));
        doNothing().when(petMapper).updateEntityFromDto(updateDTO, pet);
        given(petRepository.save(pet))
                .willReturn(pet);
        given(petMapper.toDto(pet))
                .willReturn(updateDTO);

        // WHEN
        PetDTO result = petService.updatePet(petId, updateDTO, userId);

        // THEN
        assertNotNull(result);
        assertEquals(updateDTO, result);

        then(petRepository).should().findById(petId);
        then(petMapper).should().updateEntityFromDto(updateDTO, pet);
        then(petRepository).should().save(pet);
        then(petMapper).should().toDto(pet);
    }

    @Test
    void updatePet_shouldThrowResourceNotFoundException_whenPetNotFound() {
        // GIVEN
        PetDTO updateDTO = new PetDTO(
                petId,
                "Updated Name",
                "Updated Breed",
                "Male",
                LocalDate.of(2020, 5, 15),
                "3 years old",
                "Updated description",
                null
        );

        given(petRepository.findById(petId))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> petService.updatePet(petId, updateDTO, userId)
        );

        assertEquals("Pet not found with id: " + petId, exception.getMessage());

        then(petRepository).should().findById(petId);
        then(petMapper).should(never()).updateEntityFromDto(any(), any());
        then(petRepository).should(never()).save(any());
    }

    @Test
    void updatePet_shouldThrowUnauthorizedException_whenUserDoesNotOwnPet() {
        // GIVEN
        Long otherUserId = 2L;
        PetDTO updateDTO = new PetDTO(
                petId,
                "Updated Name",
                "Updated Breed",
                "Male",
                LocalDate.of(2020, 5, 15),
                "3 years old",
                "Updated description",
                null
        );

        given(petRepository.findById(petId))
                .willReturn(Optional.of(pet));

        // WHEN & THEN
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> petService.updatePet(petId, updateDTO, otherUserId)
        );

        assertEquals("You don't have permission to update this pet", exception.getMessage());

        then(petRepository).should().findById(petId);
        then(petMapper).should(never()).updateEntityFromDto(any(), any());
        then(petRepository).should(never()).save(any());
    }

    // DELETE PET TESTS

    @Test
    void deletePet_shouldDeletePetSuccessfully() {
        // GIVEN
        given(petRepository.findById(petId))
                .willReturn(Optional.of(pet));
        doNothing().when(petRepository).delete(pet);

        // WHEN
        assertDoesNotThrow(() -> petService.deletePet(petId, userId));

        // THEN
        then(petRepository).should().findById(petId);
        then(petRepository).should().delete(pet);
    }

    @Test
    void deletePet_shouldThrowResourceNotFoundException_whenPetNotFound() {
        // GIVEN
        given(petRepository.findById(petId))
                .willReturn(Optional.empty());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> petService.deletePet(petId, userId)
        );

        assertEquals("Pet not found with id: " + petId, exception.getMessage());

        then(petRepository).should().findById(petId);
        then(petRepository).should(never()).delete(any());
    }

    @Test
    void deletePet_shouldThrowUnauthorizedException_whenUserDoesNotOwnPet() {
        // GIVEN
        Long otherUserId = 2L;

        given(petRepository.findById(petId))
                .willReturn(Optional.of(pet));

        // WHEN & THEN
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> petService.deletePet(petId, otherUserId)
        );

        assertEquals("You don't have permission to delete this pet", exception.getMessage());

        then(petRepository).should().findById(petId);
        then(petRepository).should(never()).delete(any());
    }

    // ADDITIONAL EDGE CASE TESTS

    @Test
    void createPet_shouldHandlePetWithMinimalInfo() {
        // GIVEN
        PetDTO minimalPetDTO = new PetDTO(
                null,
                "Rex",
                null,
                null,
                null,
                null,
                null,
                null
        );

        Pet minimalPet = Pet.builder()
                .name("Rex")
                .build();

        Pet savedMinimalPet = Pet.builder()
                .id(13L)
                .user(user)
                .name("Rex")
                .build();

        PetDTO savedMinimalDTO = new PetDTO(
                13L,
                "Rex",
                null,
                null,
                null,
                null,
                null,
                null
        );

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));
        given(petMapper.toEntity(minimalPetDTO))
                .willReturn(minimalPet);
        given(petRepository.save(minimalPet))
                .willReturn(savedMinimalPet);
        given(petMapper.toDto(savedMinimalPet))
                .willReturn(savedMinimalDTO);

        // WHEN
        PetDTO result = petService.createPet(minimalPetDTO, userId);

        // THEN
        assertNotNull(result);
        assertEquals(13L, result.getId());
        assertEquals("Rex", result.getName());
        assertNull(result.getBreed());
        assertNull(result.getGender());

        then(userRepository).should().findById(userId);
        then(petRepository).should().save(minimalPet);
    }

    @Test
    void getAllPetsByUserId_shouldMaintainOrderByIdDesc() {
        // GIVEN
        Pet pet1 = Pet.builder().id(1L).user(user).name("First").build();
        Pet pet2 = Pet.builder().id(2L).user(user).name("Second").build();
        Pet pet3 = Pet.builder().id(3L).user(user).name("Third").build();

        // Order should be DESC (3, 2, 1)
        List<Pet> pets = Arrays.asList(pet3, pet2, pet1);

        PetDTO dto1 = new PetDTO(1L, "First", null, null, null, null, null, null);
        PetDTO dto2 = new PetDTO(2L, "Second", null, null, null, null, null, null);
        PetDTO dto3 = new PetDTO(3L, "Third", null, null, null, null, null, null);

        given(petRepository.findByUserIdOrderByIdDesc(userId))
                .willReturn(pets);
        given(petMapper.toDto(pet3)).willReturn(dto3);
        given(petMapper.toDto(pet2)).willReturn(dto2);
        given(petMapper.toDto(pet1)).willReturn(dto1);

        // WHEN
        List<PetDTO> result = petService.getAllPetsByUserId(userId);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(3L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(1L, result.get(2).getId());

        then(petRepository).should().findByUserIdOrderByIdDesc(userId);
    }
}
