package com.summerry.admin.repository.product;

import com.summerry.admin.entity.product.ProductOptionValue;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {
    @Query("SELECT pov FROM ProductOptionValue pov WHERE pov.option.optionId = :optionId")
    Optional<ProductOptionValue> findByOptionId(@Param("optionId") Long optionId);

    @Query("SELECT pov FROM ProductOptionValue pov WHERE pov.option.optionId = :optionId")
    List<ProductOptionValue> findByOptionIds(@Param("optionId") Long optionId);
}
