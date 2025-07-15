package com.summerry.admin.dto.product;

import com.summerry.admin.entity.product.VariantValue;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDto {

    private Long variantId;

    @NotBlank(message = "SKU는 필수입니다.")
    private String sku;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private Integer stock;

    private String status;
    private Boolean deleteYn;

    @NotNull(message = "옵션값 조합은 필수입니다.")
    @Size(min = 1, message = "최소 1개의 옵션값 조합이 필요합니다.")
    private List<@NotBlank(message = "옵션값 이름은 빈 문자열일 수 없습니다.") String> valueNames;


    private List<VariantValue> variantValues;

    private List<Long> optionValueIds;

}
