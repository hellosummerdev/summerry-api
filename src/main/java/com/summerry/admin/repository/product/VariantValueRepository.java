package com.summerry.admin.repository.product;

import com.summerry.admin.entity.product.VariantValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantValueRepository extends JpaRepository<VariantValue, Long> {
    void deleteByVariantId(Long variantId);
    List<VariantValue> findByVariantId(Long variantId);
}
