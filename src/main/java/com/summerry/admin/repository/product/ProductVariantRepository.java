package com.summerry.admin.repository.product;

import com.summerry.admin.entity.product.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsBySku(String sku);

    List<ProductVariant> findByProductId(Long productId);
}
