package com.summerry.admin.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionDto {

    private Long optionId;

    @NotBlank(message = "옵션명은 필수입니다.")
    private String optionName;

    @NotNull(message = "옵션값 목록은 필수입니다.")
    @Size(min = 1, message = "최소 1개의 옵션값이 필요합니다.")
    private List<@Valid ProductOptionValueDto> values;
}