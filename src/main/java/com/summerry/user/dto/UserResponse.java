package com.summerry.user.dto;

import com.summerry.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {
    String name;
    String email;
    String phone;
    AddressResponse defaultAddress;
    LocalDateTime createdAt;
}
