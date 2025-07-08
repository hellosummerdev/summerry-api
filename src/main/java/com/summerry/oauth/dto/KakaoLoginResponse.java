package com.summerry.oauth.dto;

public record KakaoLoginResponse (
    String jwt,             // 백엔드에서 발급하는 jwt
    String kakaoAccessToken // 카카오에서 받은 access token (로그아웃에 사용)
) {}
