package com.fastcampus.sns.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fastcampus.sns.controller.request.UserJoinRequest;
import com.fastcampus.sns.controller.request.UserLoginRequest;
import com.fastcampus.sns.exception.ErrorCode;
import com.fastcampus.sns.exception.SnsApplicationException;
import com.fastcampus.sns.model.User;
import com.fastcampus.sns.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc // for API Test
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@Test
	void 회원가입() throws Exception {
		String username = "username";
		String password = "password";

		when(userService.join(username, password)).thenReturn(mock(User.class));

		mockMvc.perform(post("/api/v1/users/join")
			.contentType(MediaType.APPLICATION_JSON)
			// TODO : add request body
			.content(objectMapper.writeValueAsBytes(new UserJoinRequest(username, password)))
		).andDo(print())
		.andExpect(status().isOk());

	}

	@Test
	void 회원가입시_이미_가입된_username인_경우_에러반환() throws Exception {
		String username = "username";
		String password = "password";

		when(userService.join(username, password)).thenThrow(new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME));

		mockMvc.perform(post("/api/v1/users/join")
			.contentType(MediaType.APPLICATION_JSON)
			// TODO : add request body
			.content(objectMapper.writeValueAsBytes(new UserJoinRequest(username, password)))
		).andDo(print())
			.andExpect(status().isConflict());
	}

	@Test
	void 로그인() throws Exception {
		String username = "username";
		String password = "password";

		when(userService.login(username, password)).thenReturn("test_token");

		mockMvc.perform(post("/api/v1/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				// TODO : add request body
				.content(objectMapper.writeValueAsBytes(new UserLoginRequest(username, password)))
			).andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	void 로그인시_회원가입이_안된_username일_경우_에러반환() throws Exception {
		String username = "username";
		String password = "password";

		when(userService.login(username, password)).thenThrow(new SnsApplicationException(ErrorCode.USER_NOT_FOUND));

		mockMvc.perform(post("/api/v1/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				// TODO : add request body
				.content(objectMapper.writeValueAsBytes(new UserLoginRequest(username, password)))
			).andDo(print())
			.andExpect(status().isNotFound());
	}

	@Test
	void 로그인시_password가_다를_경우_에러반환() throws Exception {
		String username = "username";
		String password = "password";

		when(userService.login(username, password)).thenThrow(new SnsApplicationException(ErrorCode.INVALID_PASSWORD));

		mockMvc.perform(post("/api/v1/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				// TODO : add request body
				.content(objectMapper.writeValueAsBytes(new UserLoginRequest(username, password)))
			).andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	void 알람기능() throws Exception {
		when(userService.alarmList(any(), any())).thenReturn(Page.empty());

		mockMvc.perform(get("/api/v1/users/alarm")
				.contentType(MediaType.APPLICATION_JSON)
			).andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@WithAnonymousUser
	void 알람리스트요청시_로그인하지_않은경우() throws Exception {
		when(userService.alarmList(any(), any())).thenReturn(Page.empty());

		mockMvc.perform(get("/api/v1/users/alarm")
				.contentType(MediaType.APPLICATION_JSON)
			).andDo(print())
			.andExpect(status().isUnauthorized());
	}
}
