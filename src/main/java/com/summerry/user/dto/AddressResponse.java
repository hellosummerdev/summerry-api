package com.summerry.user.dto;

import com.summerry.user.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddressResponse {
    Long addressId;
    String receiverName;
    String phone;
    String addressLine1;
    String addressLine2;
    String postalCode;
    Boolean isDefault;
}
