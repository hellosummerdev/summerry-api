package com.summerry.admin.repository.product;

import com.summerry.admin.entity.product.VariantValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VariantValueRepository extends JpaRepository<VariantValue, Long> {
    @Modifying
    @Query("DELETE FROM VariantValue vv WHERE vv.productVariant.variantId = :variantId")
    void deleteByVariantId(@Param("variantId") Long variantId);

    @Query("SELECT po FROM VariantValue po WHERE po.productVariant.variantId = :variantId")
    List<VariantValue> findByVariantId(@Param("variantId") Long variantId);
}
