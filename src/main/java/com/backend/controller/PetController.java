package com.backend.controller;


import com.backend.dto.PetDTO;
import com.backend.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pet Management", description = "API for managing user pet profiles")
@SecurityRequirement(name = "Bearer Authentication")
public class PetController {

    private final PetService petService;


    @Operation(summary = "Get all user pets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pets"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
    })
    @GetMapping
    public ResponseEntity<List<PetDTO>> getAllPets(@RequestParam Long userId) {
        List<PetDTO> pets = petService.getAllPetsByUserId(userId);
        return ResponseEntity.ok(pets);
    }


    @Operation(summary = "Create a new pet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pet successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid pet data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping
    public ResponseEntity<PetDTO> createPet(@RequestBody PetDTO petDto,
                                            @RequestParam Long userId) {
        PetDTO createdPet = petService.createPet(petDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPet);
    }


    @Operation(summary = "Update a new pet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pet successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid pet data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PetDTO> updatePet(@PathVariable Long id,
                                            @RequestBody PetDTO petDto,
                                            @RequestParam Long userId) {
        PetDTO updatedPet = petService.updatePet(id, petDto, userId);
        return ResponseEntity.ok(updatedPet);
    }


    @Operation(summary = "Delete a pet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own this pet"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id,
                                          @RequestParam Long userId) {
        petService.deletePet(id, userId);
        return ResponseEntity.ok().build();
    }
}
