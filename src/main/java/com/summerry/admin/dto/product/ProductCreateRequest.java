package com.summerry.admin.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @NotBlank(message = "상품 설명은 필수입니다.")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "썸네일 URL은 필수입니다.")
    private String thumbnailUrl;

    @NotNull(message = "옵션 정보는 필수입니다.")
    @Size(min = 1, message = "최소 1개의 옵션이 필요합니다.")
    private List<ProductOptionDto> options;

    @NotNull(message = "상품 조합 정보는 필수입니다.")
    @Size(min = 1, message = "최소 1개의 상품 조합이 필요합니다.")
    private List<@Valid ProductVariantDto> variants;
}
