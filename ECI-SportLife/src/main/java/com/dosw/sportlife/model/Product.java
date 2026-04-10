package com.dosw.sportlife.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String category;

    private Double price;

    private Integer stock;

    @ElementCollection
    private List<String> images;

    private String status = "ACTIVE";
}
