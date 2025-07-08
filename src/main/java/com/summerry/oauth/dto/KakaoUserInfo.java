package com.summerry.oauth.dto;

public record KakaoUserInfo(
        String kakaoId,     // 카카오에서 받은 사용자 고유 ID
        String email,       // 이메일 (없을 수 있음)
        String nickname     // 카카오 닉네임 (or 이름으로 매핑)
) {}
