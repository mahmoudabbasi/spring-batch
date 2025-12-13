package com.example.batchprocessing.service.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
@Entity
@Table(name = "PRODUCT" , schema = "TESTDB")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PRODUCT_ID", unique = true, nullable = false)
    private Long productId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "BRAND_NAME")
    private String brandName;

    @Column(name = "PRICE", nullable = false)
    private double price;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "CREATED_ON", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Column(name = "UPDATED_ON")
    private LocalDateTime updatedDate;

    @Column(name = "STATUS", nullable = true)
    private String status;

    public Product(Long productId, String name, String brandName, double price, String description, String createdBy, LocalDateTime createdDate, String status) {
        this.productId = productId;
        this.name = name;
        this.brandName = brandName;
        this.price = price;
        this.description = description;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.status = status;
    }

}
