package com.summerry.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerry.config.TestSecurityConfig;
import com.summerry.user.dto.SignupRequest;
import com.summerry.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signup_o() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setPassword("1234");
        request.setEmail("test@test.com");
        request.setPhone("010-1234-5678");
        request.setName("테스트유저");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입이 완료되었습니다."));
    }
}
