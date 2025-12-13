package com.example.batchprocessing.service.catalog.processor;

import com.example.batchprocessing.service.catalog.domain.Product;
import com.example.batchprocessing.service.catalog.dto.ProductDTO;
import com.example.batchprocessing.service.catalog.dto.ProductWrapper;
import com.example.batchprocessing.service.catalog.error.BrandNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;
import java.util.Objects;



public class ProductItemProcessor implements ItemProcessor<ProductDTO, ProductWrapper> {

    private static final Logger log = LoggerFactory.getLogger(ProductItemProcessor.class);

    private static final String ACTIVE_STATUS = "ACTIVE";

    private static final String ADMIN_USER = "admin";

    @Override
    public ProductWrapper process(ProductDTO productDTO) throws Exception {
        if (Objects.isNull(productDTO)) return null;

        Product product = new Product(
                productDTO.productId(),
                productDTO.productName(),
                productDTO.productBrand(),
                productDTO.price(),
                productDTO.description(),
                "admin",
                LocalDateTime.now(),
                "ACTIVE"
        );

        ProductAction action;

        switch (productDTO.productBrand()) {
            case "Parx":
                action = ProductAction.INSERT;
                break;
            case "SPYKAR":
                action = ProductAction.UPDATE;
                break;
            case "YAK YAK":
                action = ProductAction.DELETE;
                break;
            default:
                // برای برندهای دیگر می‌توانیم insert کنیم یا skip کنیم
                action = ProductAction.INSERT;
                break;
        }

        return new ProductWrapper(product, action);
    }

    /**
     * Process the provided ProductDTO and convert it to Product
     *
     * @param productDTO ProductDTO
     * @return Product
     * @throws Exception
     */
//    @Override
//    public Product process(ProductDTO productDTO) throws Exception {
//        if (Objects.nonNull(productDTO)) {
//            log.debug("Processing data for ProductDTO: {}", productDTO);
//            if (productDTO.productBrand() == null || productDTO.productBrand().isEmpty()) {
//                throw new BrandNotFoundException("Brand not found for product: " + productDTO.productName());
//            }
//
//            return new Product(productDTO.productId(), productDTO.productName(), productDTO.productBrand(),
//                    productDTO.price(), productDTO.description(),
//                    ADMIN_USER, java.time.LocalDateTime.now(), ACTIVE_STATUS);
//        } else {
//            log.error("Error: While processing data: ProductDTO is null");
//            return null;
//        }
//    }
}
