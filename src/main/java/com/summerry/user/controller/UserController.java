package com.summerry.user.controller;

import com.summerry.global.security.JwtUtil;
import com.summerry.global.security.UserDetailsImpl;
import com.summerry.user.dto.*;
import com.summerry.user.entity.Address;
import com.summerry.user.entity.User;
import com.summerry.user.repository.AddressRepository;
import com.summerry.user.service.AddressService;
import com.summerry.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddressService addressService;
    private final JwtUtil jwtUtil;
    private final AddressRepository addressRepository;

    @Operation(summary = "회원가입 API", description = "유저 정보를 받아 회원가입 처리합니다.")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "로그인 API", description = "유저 정보를 받아 로그인 처리합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @Operation(summary = "로그아웃 API", description = "유저 토큰 정보를 받아 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // 클라이언트가 보낸 Access Token을 추출
        String accessToken = jwtUtil.resolveToken(request);

        // 유효성 검사만 하고, 서버는 별도 저장 X 클라이언트가 localStorage에서 토큰을 삭제
        if (accessToken != null && jwtUtil.validateToken(accessToken)) {
            return ResponseEntity.ok("로그아웃 완료 (Access Token 삭제)");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 토큰");
        }
    }

    @Operation(
        summary = "유저 정보 조회 API",
        description = "현재 로그인한 유저의 이름, 이메일, 휴대폰 번호, 기본 배송지 정보를 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        // 1. 기본 배송지 조회
        Address defaultAddress = addressRepository.findByUserAndIsDefaultTrue(user);

        // 2. AddressResponse로 반환
        AddressResponse addressResponse = defaultAddress != null ? new AddressResponse(
            defaultAddress.getAddressId(),
            defaultAddress.getReceiverName(),
            defaultAddress.getPhone(),
            defaultAddress.getAddressLine1(),
            defaultAddress.getAddressLine1(),
            defaultAddress.getPostalCode(),
            defaultAddress.getIsDefault()
        ) : null;

        // 3. UserResponse에 넣어서 응답
        return ResponseEntity.ok(new UserResponse(
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            addressResponse,
            user.getCreatedAt())
        );
    }

    @Operation(
        summary = "유저 정보 부분 수정 API",
        description = "이름, 이메일, 휴대폰 번호 중 변경하고 싶은 항목만 수정할 수 있습니다."
    )
    @PatchMapping("/me")
    public ResponseEntity<?> updateUserInfo(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody @Valid UserUpdateRequest userUpdateRequest
    ) {
        userService.UpdateUserInfo(userDetails.getUser(), userUpdateRequest);
        return ResponseEntity.ok("유저 정보 수정 완료");
    }


    @Operation(summary = "비밀번호 수정 API", description = "유저의 비밀번호를 수정합니다.")
    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid PasswordUpdateRequest passwordUpdateRequest
    ) {
        userService.updatePassword(userDetails.getUser(), passwordUpdateRequest);
        return ResponseEntity.ok("비밀번호 수정 완료");
    }


    @Operation(summary = "배송지 조회 API", description = "유저의 배송지를 조회합니다.")
    @GetMapping("/me/address")
    public ResponseEntity<?> getAddress(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<AddressResponse> addressList = addressService.getAddresses(userDetails.getUser());
        return ResponseEntity.ok(addressList);
    }

    @Operation(summary = "배송지 등록 API", description = "유저의 배송지를 등록합니다.")
    @PostMapping("/me/address")
    public ResponseEntity<?> createAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid AddressRequest addressRequest
    ) {
        addressService.createAddress(userDetails.getUser(), addressRequest);
        return ResponseEntity.ok("배송지 등록 완료");
    }

    @Operation(summary = "배송지 수정 API", description = "유저의 배송지를 수정합니다.")
    @PatchMapping("/me/address/{addressId}")
    public ResponseEntity<?> updateAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long addressId,
            @RequestBody @Valid AddressRequest addressRequest
    ) {
        addressService.updateAddress(userDetails.getUser(), addressId, addressRequest);
        return ResponseEntity.ok("배송지 수정 완료");
    }

    @Operation(summary = "배송지 삭제 API", description = "유저의 배송지를 삭제(soft delete)합니다.")
    @DeleteMapping("/me/address/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long addressId
    ) {
        addressService.deleteAddress(userDetails.getUser(), addressId);
        return ResponseEntity.ok("배송지 삭제 완료");
    }

}
