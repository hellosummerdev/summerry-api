package com.summerry.admin.dto.product;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionValueDto {

    private Long valueId;

    @NotBlank(message = "옵션값 이름은 필수입니다.")
    private String valueName;

    @NotNull(message = "옵션 추가금액은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "추가금액은 0 이상이어야 합니다.")
    private BigDecimal adjustPrice;
}
