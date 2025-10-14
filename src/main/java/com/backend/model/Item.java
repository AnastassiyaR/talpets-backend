package com.backend.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
    @Id
    long id;
    String name;
    double price;
}
