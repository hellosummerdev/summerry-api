package com.summerry.admin.repository.product;

import com.summerry.admin.entity.product.ProductOption;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    @Query("SELECT po FROM ProductOption po WHERE po.product.productId = :productId")
    List<ProductOption> findByProductId(@Param("productId") Long productId);
}
