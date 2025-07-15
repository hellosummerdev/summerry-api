package com.summerry.admin.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {
    private Long productId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    private String thumbnailUrl;

    @NotNull
    private Long categoryId;

    private List<@Valid ProductOptionDto> options;

    private List<@Valid ProductVariantDto> variants;

}
