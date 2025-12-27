package com.backend.repository;


import com.backend.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByUserIdOrderByIdDesc(Long userId);
}
