package com.backend.model;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false)
    private SizeType size;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet", nullable = false)
    private PetType pet;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "img")
    private String img;

    @Column(name = "color")
    private String color;

}

