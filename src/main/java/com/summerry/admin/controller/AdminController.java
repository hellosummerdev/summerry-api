package com.summerry.admin.controller;

import com.summerry.admin.dto.AdminLoginRequest;
import com.summerry.admin.dto.AdminLoginResponse;
import com.summerry.admin.dto.AdminSignupRequest;
import com.summerry.admin.service.AdminService;
import com.summerry.global.security.JwtUtil;
import com.summerry.user.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "회원가입 API", description = "셀러 정보를 받아 회원가입 요청을 처리합니다.")
    @PostMapping("/signup")
    public ResponseEntity<?> signup (@RequestBody @Valid AdminSignupRequest adminSignupRequest) {
        adminService.signup(adminSignupRequest);
        return ResponseEntity.ok("셀러 회원가입 요청이 완료되었습니다.");
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "셀러 관리자 승인 API", description = "SUPER_ADMIN만 호출할 수 있으며, 해당 ID의 셀러 계정을 승인 처리합니다.")
    @PatchMapping("/approve/{adminId}")
    public ResponseEntity<?> approveAdmin(@PathVariable("adminId") Long adminId) {
        adminService.approveAdmin(adminId);
        return ResponseEntity.ok("셀러 계정이 승인되었습니다.");
    }

    @Operation(summary = "로그인 API", description = "셀러 정보를 받아 로그인 처리합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃 API", description = "셀러 토큰 정보를 받아 로그아웃 처리합니다.")
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

}
