package com.summerry.admin.service;

import com.summerry.admin.dto.AdminLoginRequest;
import com.summerry.admin.dto.AdminLoginResponse;
import com.summerry.admin.dto.AdminSignupRequest;
import com.summerry.admin.entity.Admin;
import com.summerry.admin.entity.AdminRole;
import com.summerry.admin.repository.AdminRepository;
import com.summerry.global.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(AdminSignupRequest request) {
        if (adminRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        AdminRole role = request.getRole();
        if (role == null) {
            throw new IllegalArgumentException("관리자 권한 정보를 입력해주세요.");
        }
        
        // Admin builder 객체 생성
        Admin.AdminBuilder adminBuilder  = Admin.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(request.getEmail())
                .phone(request.getPhone())
                .name(request.getName())
                .role(role)
                .deleteYn(false)
                .createdAt(LocalDateTime.now())
                .marketName(request.getMarketName())
                .customerCenterPhone(request.getCustomerCenterPhone());

        // 셀러 계정 필수값 확인
        if (role == AdminRole.SELLER_ADMIN) {
            if(!StringUtils.hasText(request.getMarketName()) || !StringUtils.hasText(request.getCustomerCenterPhone())) {
                throw new IllegalArgumentException("마켓명과 고객센터 번호는 필수입니다.");
            }
            adminBuilder
                    .marketName(request.getMarketName())
                    .customerCenterPhone(request.getCustomerCenterPhone());
        }
        // DB에 저장
        adminRepository.save(adminBuilder.build());
    }

    @Transactional
    public void approveAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if(Boolean.TRUE.equals(admin.getDeleteYn())) {
            throw new IllegalStateException("탈퇴한 계정입니다.");
        }

        if(Boolean.TRUE.equals(admin.getApproveYn())) {
            throw new IllegalStateException("이미 승인된 셀러 계정입니다.");
        }

        admin.setApproveYn(true);
    }


    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if(Boolean.TRUE.equals(admin.getDeleteYn())) {
            throw new IllegalStateException("탈퇴한 계정입니다.");
        }

        if(Boolean.FALSE.equals(admin.getApproveYn())) {
            throw new IllegalStateException("가입 승인되지 않은 계정입니다.");
        }

        String token = jwtUtil.generateToken(admin);
        return new AdminLoginResponse(token, admin.getUsername(), admin.getRole().name());
    }
}
