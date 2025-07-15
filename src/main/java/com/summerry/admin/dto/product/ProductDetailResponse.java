package com.summerry.admin.dto.product;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String thumbnailUrl;
    private String categoryName;
    private List<ProductOptionDto> options;
    private List<ProductVariantDto> variants;
}

