package com.summerry.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {

    @NotBlank
    private Long addressId;

    @NotBlank
    private String receiverName;

    @NotBlank
    private String phone;

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    @NotBlank
    private String postalCode;

    private Boolean isDefault;

}
