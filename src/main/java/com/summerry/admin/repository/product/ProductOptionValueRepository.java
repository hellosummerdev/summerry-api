package com.summerry.admin.repository.product;

import com.summerry.admin.entity.product.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {
    Optional<ProductOptionValue> findByOptionId(Long optionId);
    List<ProductOptionValue> findByOptionIds(Long optionId);
}
