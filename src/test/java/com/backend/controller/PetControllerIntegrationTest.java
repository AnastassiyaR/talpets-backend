package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import com.backend.dto.PetDTO;
import com.backend.model.Pet;
import com.backend.model.User;
import com.backend.repository.PetRepository;
import com.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class PetControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Pet pet;

    @BeforeEach
    void setUp() {
        petRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .email("user@mail.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .build();
        user = userRepository.save(user);

        pet = Pet.builder()
                .name("Buddy")
                .breed("Golden Retriever")
                .gender("Male")
                .birthday(LocalDate.of(2020, 5, 15))
                .age("3 years")
                .description("Friendly dog")
                .user(user)
                .build();
        pet = petRepository.save(pet);
    }

    // GET
    @Test
    void getAllPets_shouldReturnUserPets() throws Exception {
        mockMvc.perform(get("/api/pets")
                        .param("userId", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Buddy"));
    }

    // CREATE

    @Test
    void createPet_shouldCreatePetSuccessfully() throws Exception {
        PetDTO dto = new PetDTO(
                null,
                "Charlie",
                "Labrador",
                "Male",
                LocalDate.of(2021, 3, 10),
                "2 years",
                "Very active",
                null
        );

        mockMvc.perform(post("/api/pets")
                        .param("userId", user.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Charlie"));

        assertEquals(2, petRepository.count());
    }

    @Test
    void createPet_shouldReturn404_whenUserNotFound() throws Exception {
        PetDTO dto = new PetDTO(
                null,
                "Ghost",
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/pets")
                        .param("userId", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User not found with id: 999"));
    }

    // UPDATE

    @Test
    void updatePet_shouldUpdatePetSuccessfully() throws Exception {
        PetDTO updateDto = new PetDTO(
                null,
                "Buddy Updated",
                "Retriever",
                "Male",
                LocalDate.of(2020, 5, 15),
                "4 years",
                "Calmer now",
                null
        );

        mockMvc.perform(put("/api/pets/{id}", pet.getId())
                        .param("userId", user.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buddy Updated"))
                .andExpect(jsonPath("$.age").value("4 years"));
    }

    @Test
    void updatePet_shouldReturn403_whenUserIsNotOwner() throws Exception {
        User otherUser = userRepository.save(
                User.builder()
                        .email("other@mail.com")
                        .password("password")
                        .firstName("Jane")
                        .lastName("Doe")
                        .build()
        );

        PetDTO updateDto = new PetDTO();
        updateDto.setName("Hack attempt");

        mockMvc.perform(put("/api/pets/{id}", pet.getId())
                        .param("userId", otherUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You don't have permission to update this pet"));
    }

    // DELETE

    @Test
    void deletePet_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/pets/{id}", pet.getId())
                        .param("userId", user.getId().toString()))
                .andExpect(status().isOk());

        assertEquals(0, petRepository.count());
    }

    @Test
    void deletePet_shouldReturn403_whenUserIsNotOwner() throws Exception {
        User otherUser = userRepository.save(
                User.builder()
                        .email("other@mail.com")
                        .password("password")
                        .firstName("Jane")
                        .lastName("Doe")
                        .build()
        );

        mockMvc.perform(delete("/api/pets/{id}", pet.getId())
                        .param("userId", otherUser.getId().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You don't have permission to delete this pet"));
    }

    @Test
    void deletePet_shouldReturn404_whenPetNotFound() throws Exception {
        mockMvc.perform(delete("/api/pets/{id}", 999L)
                        .param("userId", user.getId().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Pet not found with id: 999"));
    }
}
