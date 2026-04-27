package com.platform.ecommerce.category.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Self reference (parent)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    // Children list
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children;
}