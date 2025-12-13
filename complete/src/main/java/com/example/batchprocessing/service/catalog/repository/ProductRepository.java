package com.example.batchprocessing.service.catalog.repository;

import com.example.batchprocessing.service.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Product findByProductId(Long productId);


    List<Product> findAllByProductIdIn(List<Long> ids);
    void deleteAllByProductIdIn(List<Long> ids);


}
