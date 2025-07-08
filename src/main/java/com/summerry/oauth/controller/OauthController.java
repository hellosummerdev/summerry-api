package com.summerry.oauth.controller;

import com.summerry.oauth.dto.KakaoLoginResponse;
import com.summerry.oauth.service.KakaoOauthService;
import com.summerry.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OauthController {

    private final KakaoOauthService kakaoOauthService;
    private final UserService userService;

    @GetMapping("/kakao/login")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code) {
        KakaoLoginResponse response = userService.kakaoLogin(code);
        return ResponseEntity.ok().body(Map.of(
            "token", response.jwt(),
            "kakaoAccessToken", response.kakaoAccessToken())
        );
    }

    @PostMapping("/kakao/login")
    public ResponseEntity<?> kakaoLogout(@RequestHeader("Authorization") String authorizationHeader) {
        // Bearer 토큰에서 실제 access token만 추출
        String accessToken = authorizationHeader.replace("Bearer ", "");
        kakaoOauthService.logout(accessToken);
        return ResponseEntity.ok("카카오 로그아웃 성공");
    }
}
