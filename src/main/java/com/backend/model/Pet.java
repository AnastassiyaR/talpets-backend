package com.backend.model;


import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name")
    private String name;

    @Column(name = "breed")
    private String breed;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "age")
    private String age;

    @Column(name = "description")
    private String description;

    @Column(name = "photo")
    private String photo;
}
