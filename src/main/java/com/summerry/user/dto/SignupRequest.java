package com.summerry.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequest {
    @NotBlank(message = "아이디는 필수 입력값입니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 16)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*]).{8,16}$", message = "비밀번호는 숫자와 특수문자를 각각 1개 이상 포함해야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    @NotBlank(message = "휴대폰 번호는 필수 입력값입니다.")
    private String phone;

}
