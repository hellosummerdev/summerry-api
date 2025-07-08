package com.summerry.user.service;

import com.summerry.global.security.JwtUtil;
import com.summerry.oauth.service.KakaoOauthService;
import com.summerry.user.dto.PasswordUpdateRequest;
import com.summerry.user.dto.SignupRequest;
import com.summerry.user.dto.UserUpdateRequest;
import com.summerry.user.entity.User;
import com.summerry.user.mapper.AddressMapper;
import com.summerry.user.repository.AddressRepository;
import com.summerry.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private UserService userService;
    @Mock
    private KakaoOauthService kakaoOauthService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtUtil = mock(JwtUtil.class);
        userService = new UserService(userRepository,passwordEncoder, jwtUtil, kakaoOauthService);
    }

    @Test
    @DisplayName("중복 아이디(username) 검사")
    void signup_id_exist() {
        // given
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");

        // DB에 이미 존재한다고 가정
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(User.builder().username("testuser").build()));

        // then
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");
    }

    @Test
    @DisplayName("중복 이메일(email) 검사")
    void signup_email_exist() {
        // given
        SignupRequest request = new SignupRequest();
        request.setEmail("test@test.com");

        // DB에 이미 존재한다고 가정
        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(true);

        // then
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

    }

    @Test
    @DisplayName("가입 성공")
    void signup_should_succeed() {
        // given
        SignupRequest request = new SignupRequest();
        request.setUsername("usertest");
        request.setPassword("1234");
        request.setEmail("test@test.com");
        request.setPhone("010-1234-5678");
        request.setName("홍길동");

        // when
        userService.signup(request);

        // then
        verify(userRepository).save(any(User.class)); // 저장 여부 확인
    }

    @Test
    @DisplayName("이름, 이메일, 전화번호를 모두 수정")
    void updateAllFieldsSuccess() {
        // given
        User user = User.builder()
                .userId(1L)
                .name("기존이름")
                .email("old@email.com")
                .phone("01012345678")
                .build();

        UserUpdateRequest dto = new UserUpdateRequest();
        dto.setName("유저");
        dto.setEmail("new@example.com");
        dto.setPhone("01099998888");

        when(userRepository.existsByEmailAndUserIdNot("new@example.com", 1L)).thenReturn(false);
        when(userRepository.existsByPhoneAndUserIdNot("01099998888", 1L)).thenReturn(false);

        // when
        userService.UpdateUserInfo(user, dto);

        // then
        assertEquals("유저", user.getName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("01099998888", user.getPhone());
    }

    @Test
    @DisplayName("중복 이메일로 변경 시도시 예외 발생")
    void updateDuplicateEmailThrowsException() {
        // given
        User user = User.builder()
                .userId(1L)
                .name("기존이름")
                .email("duplicate@email.com")
                .phone("01012345678")
                .build();

        // when
        UserUpdateRequest dto = new UserUpdateRequest();
        dto.setEmail("duplicate@email.com");

        when(userRepository.existsByEmailAndUserIdNot(eq(dto.getEmail()), anyLong())).thenReturn(true);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> userService.UpdateUserInfo(user, dto));

        // then
        assertEquals("중복된 이메일입니다.", e.getMessage());
    }

    @Test
    @DisplayName("중복 전화번호로 변경 시도시 예외 발생")
    void updateDuplicatePhoneThrowsException() {
        // given
        User user = User.builder()
                .userId(1L)
                .name("기존이름")
                .email("duplicate@email.com")
                .phone("01099998888")
                .build();

        // when
        UserUpdateRequest dto = new UserUpdateRequest();
        dto.setPhone("01099998888");

        when(userRepository.existsByPhoneAndUserIdNot(eq(dto.getPhone()), anyLong())).thenReturn(true);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> userService.UpdateUserInfo(user, dto));

        // then
        assertEquals("중복된 전화번호입니다.", e.getMessage());
    }

    @Test
    @DisplayName("입력값 없이 수정 시 아무 변화 없음")
    void updateWithEmptyDtoDoesNothing() {
        // given
        User user = User.builder()
                .userId(1L)
                .name("기존이름")
                .email("old@email.com")
                .phone("01012345678")
                .build();
        UserUpdateRequest dto = new UserUpdateRequest();

        userService.UpdateUserInfo(user, dto);

        assertEquals("기존이름", user.getName());
        assertEquals("old@email.com", user.getEmail());
        assertEquals("01012345678", user.getPhone());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // given
        user = User.builder()
                .userId(1L)
                .username("testuser")
                .password("encoded_old_pw")
                .build();
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setCurrentPassword("old_pw");
        request.setNewPassword("new_pw");

        when(passwordEncoder.matches("old_pw", "encoded_old_pw")).thenReturn(true);
        when(passwordEncoder.encode("new_pw")).thenReturn("encoded_new_pw");

        // when
        userService.updatePassword(user, request);

        // then
        assertEquals("encoded_new_pw", user.getPassword());
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 예외")
    void changePassword_wrongCurrentPassword_throwsException() {
        // given
        user = User.builder()
                .userId(1L)
                .username("testuser")
                .password("encoded_old_pw")
                .build();
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setCurrentPassword("wrong_pw");
        request.setNewPassword("new_pw");

        when(passwordEncoder.matches("wrong_pw", "encoded_old_pw")).thenReturn(false);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updatePassword(user, request));

        assertEquals("현재 비밀번호가 일치하지 않습니다.", exception.getMessage());
    }

}
