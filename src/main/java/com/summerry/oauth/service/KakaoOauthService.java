package com.summerry.oauth.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerry.oauth.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * kakao
 *
 */
@Service
@RequiredArgsConstructor
public class KakaoOauthService {

@Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;


    /**
     * @param code
     * @return
     */
    public String requestAccessToken(String code) {
        // 1. 요청 보낼 HTTP 클라이언트 준비
        RestTemplate restTemplate = new RestTemplate();

        // 2. 요청 헤더 설정 (application/x-www-form-urlencoded)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 3. 요청 바디에 들어갈 파라미터 구성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        // 4. 헤더 + 바디를 합쳐서 HttpEntity로 감쌈
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 5. 카카오 서버로 POST 요청 → 토큰 응답 받기
        ResponseEntity<String> response = restTemplate.postForEntity(
            "https://kauth.kakao.com/oauth/token",
            request,
            String.class
        );

        // 6. 응답 결과(JSON) 문자열 반환
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("access_token").asText(); //access_token만 추출
        } catch (Exception e) {
            throw new RuntimeException("액세스 토큰 파싱 실패", e);
        }
    }

    // 카카오 AccessToken을 이용해 사용자 정보를 조회하는 메서드
    public KakaoUserInfo getUserInfo(String accessToken) {

        // 1. Spring의 HTTP 클라이언트 객체 생성
        RestTemplate restTemplate = new RestTemplate();

        // 2. Authorization 헤더에 Bearer {access_token} 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // Authorization: Bearer {access_token}

        // 3. 헤더만 담긴 요청 객체 생성 (바디는 없음)
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 4. 카카오 사용자 정보 API 호출 (GET /v2/user/me)
        ResponseEntity<String> response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me", // 카카오 사용자 정보 요청 URL
            HttpMethod.GET,                      // HTTP 메서드는 GET
            request,                             // 헤더 포함한 요청
            String.class                         // 응답은 JSON 문자열로 받음
        );

        try {
            // 5. JSON 문자열 응답을 Jackson으로 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());

            // 6. 사용자 고유 식별자(id) 추출 (필수)
            String kakaoId = root.path("id").asText();

            // 7. 사용자 이메일 추출 (없을 수도 있음 → null 처리)
            String email = root.path("kakao_account").path("email").asText(null);

            // 8. 사용자 닉네임 추출 (없으면 기본값 "카카오사용자"로 처리)
            String nickname = root.path("properties").path("nickname").asText("kakaouser");

            // 9. 사용자 정보를 담은 DTO 반환
            return new KakaoUserInfo(kakaoId, email, nickname);

        } catch (Exception e) {
            // 11. 파싱 실패 시 예외 처리
            throw new RuntimeException("카카오 사용자 정보 파싱 실패", e);
        }
    }

    public void logout(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "https://kapi.kakao.com/v1/user/logout",
            request,
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("카카오 로그아웃 실패");
        }
    }

}

