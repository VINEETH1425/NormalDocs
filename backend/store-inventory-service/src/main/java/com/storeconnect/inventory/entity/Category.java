package com.storeconnect.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories", schema = "inventory_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String description;
}
