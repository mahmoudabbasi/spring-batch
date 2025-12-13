package com.example.batchprocessing.service.catalog.writer;

import com.example.batchprocessing.service.catalog.domain.Product;
import com.example.batchprocessing.service.catalog.dto.ProductWrapper;
import com.example.batchprocessing.service.catalog.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductItemWriter implements ItemWriter<ProductWrapper> {

    private static final Logger log = LoggerFactory.getLogger(ProductItemWriter.class);

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void write(Chunk<? extends ProductWrapper> chunk) throws Exception {
        List<Product> toInsert = new ArrayList<>();
        List<Product> toUpdate = new ArrayList<>();
        List<Long> toDeleteIds = new ArrayList<>();

        for (ProductWrapper wrapper : chunk.getItems()) {
            switch (wrapper.action()) {
                case INSERT -> toInsert.add(wrapper.product());
                case UPDATE -> toUpdate.add(wrapper.product());
                case DELETE -> toDeleteIds.add(wrapper.product().getProductId());
            }
        }

        // Batch Insert
//        if (!toInsert.isEmpty()) {
//            productRepository.saveAll(toInsert);
//        }

        List<Long> insertIds = toInsert.stream().map(Product::getProductId).toList();
        List<Product> existingّInsert = productRepository.findAllByProductIdIn(insertIds);

        Set<Long> existingIds = existingّInsert.stream().map(Product::getProductId).collect(Collectors.toSet());
        List<Product> newRecords = toInsert.stream()
                .filter(p -> !existingIds.contains(p.getProductId()))
                .toList();

        if (!newRecords.isEmpty()) {
            productRepository.saveAll(newRecords);
        }


        // Batch Update
        if (!toUpdate.isEmpty()) {
            List<Long> updateIds = toUpdate.stream()
                    .map(Product::getProductId)
                    .toList();

            List<Product> existingProducts = productRepository.findAllByProductIdIn(updateIds);

            Map<Long, Product> updateMap = toUpdate.stream()
                    .collect(Collectors.toMap(Product::getProductId, p -> p));

            for (Product existing : existingProducts) {
                Product updated = updateMap.get(existing.getProductId());
                existing.setName(updated.getName());
                existing.setPrice(updated.getPrice());
                existing.setBrandName(updated.getBrandName());
                existing.setDescription(updated.getDescription());
                existing.setUpdatedBy("admin");
                existing.setUpdatedDate(LocalDateTime.now());
            }

            productRepository.saveAll(existingProducts);
        }

        // Batch Delete
        if (!toDeleteIds.isEmpty()) {
            productRepository.deleteAllByProductIdIn(toDeleteIds);
        }
    }

    /**
     * Write the provided Product items to the database
     *
     * @param items Chunk<? extends Product>
     * @throws Exception
     */
//    @Override
//    public void write(Chunk<? extends Product> items) throws Exception {
//        log.info("Writing data for Product: {}", items.getItems().size());
//        productRepository.saveAll(items.getItems());
//    }


}
