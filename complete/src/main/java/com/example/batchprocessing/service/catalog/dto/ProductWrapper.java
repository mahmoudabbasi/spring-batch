package com.example.batchprocessing.service.catalog.dto;

import com.example.batchprocessing.service.catalog.domain.Product;
import com.example.batchprocessing.service.catalog.processor.ProductAction;

public record ProductWrapper (Product product, ProductAction action) {
}
