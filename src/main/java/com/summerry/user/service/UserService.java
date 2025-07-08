package com.summerry.user.service;

import com.summerry.global.security.JwtUtil;
import com.summerry.oauth.dto.KakaoLoginResponse;
import com.summerry.oauth.dto.KakaoUserInfo;
import com.summerry.oauth.service.KakaoOauthService;
import com.summerry.user.dto.*;
import com.summerry.user.entity.Role;
import com.summerry.user.entity.SocialType;
import com.summerry.user.entity.User;
import com.summerry.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final KakaoOauthService kakaoOauthService;

    @Transactional
    public void signup(SignupRequest signupRequest) {
        if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // User 객체 생성
        User user = User.builder()
                .username(signupRequest.getUsername())
                .password(encodedPassword)
                .email(signupRequest.getEmail())
                .phone(signupRequest.getPhone())
                .name(signupRequest.getName())
                .role(Role.USER)
                .build();

        // DB에 저장
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }

    @Transactional
    public KakaoLoginResponse kakaoLogin(String code) {
        // 1. 토큰 요청
        String kakaoAccessToken = kakaoOauthService.requestAccessToken(code);

        // 2. 사용자 정보 요청
        KakaoUserInfo kakaoUserInfo = kakaoOauthService.getUserInfo(kakaoAccessToken);

        // 3. 사용자 존재 여부 확인
        Optional<User> existingUser = userRepository.findBySocialTypeAndSocialId(
                SocialType.KAKAO, kakaoUserInfo.kakaoId()
        );

        User user = existingUser.orElseGet(() -> {
            // 4. 새 사용자 생성
            return userRepository.save(
                    User.builder()
                            .username("kakao_" + kakaoUserInfo.kakaoId())
                            .password("") // 소셜 로그인은 패스워드 비워도 됨
                            .name(kakaoUserInfo.nickname())
                            .email(kakaoUserInfo.email())
                            .phone(null)
                            .role(Role.USER)
                            .socialType(SocialType.KAKAO)
                            .socialId(kakaoUserInfo.kakaoId())
                            .deleteYn(false)
                            .createdBy(null) // 시스템 등록
                            .build());
        });

        // 5. JWT 발급
        String jwt = jwtUtil.generateToken(user);
        return new KakaoLoginResponse(jwt, kakaoAccessToken);
    }

    @Transactional
    public void UpdateUserInfo(User user, UserUpdateRequest updateRequest) {
        if (updateRequest.getName() != null && !updateRequest.getName().isBlank()) {
            user.setName(updateRequest.getName());
        }

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
            if (userRepository.existsByEmailAndUserIdNot(updateRequest.getEmail(), user.getUserId())) {
                throw new IllegalArgumentException("중복된 이메일입니다.");
            }
            user.setEmail(updateRequest.getEmail());
        }

        if (updateRequest.getPhone() != null && !updateRequest.getPhone().isBlank()) {
            if (userRepository.existsByPhoneAndUserIdNot(updateRequest.getPhone(), user.getUserId())) {
                throw new IllegalArgumentException("중복된 전화번호입니다.");
            }
            user.setPhone(updateRequest.getPhone());
        }
    }

    @Transactional
    public void updatePassword(User user, PasswordUpdateRequest passwordUpdateRequest) {
        if(!passwordEncoder.matches(passwordUpdateRequest.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(passwordUpdateRequest.getNewPassword()));
    }
}

