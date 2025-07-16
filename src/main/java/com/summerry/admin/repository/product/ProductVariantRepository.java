package com.summerry.admin.repository.product;

import com.summerry.admin.entity.product.ProductVariant;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsBySku(String sku);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.productId = :productId")
    List<ProductVariant> findByProductId(@Param("productId") Long productId);
}
