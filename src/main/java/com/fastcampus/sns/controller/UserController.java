package com.fastcampus.sns.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fastcampus.sns.controller.request.UserJoinRequest;
import com.fastcampus.sns.controller.request.UserLoginRequest;
import com.fastcampus.sns.controller.response.AlarmResponse;
import com.fastcampus.sns.controller.response.Response;
import com.fastcampus.sns.controller.response.UserJoinResponse;
import com.fastcampus.sns.controller.response.UserLoginResponse;
import com.fastcampus.sns.exception.ErrorCode;
import com.fastcampus.sns.exception.SnsApplicationException;
import com.fastcampus.sns.model.User;
import com.fastcampus.sns.service.AlarmService;
import com.fastcampus.sns.service.UserService;
import com.fastcampus.sns.util.ClassUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final AlarmService alarmService;

	@PostMapping("/join")
	public Response<UserJoinResponse> join(@RequestBody UserJoinRequest request) {
		User user = userService.join(request.getName(), request.getPassword());
		return Response.success(UserJoinResponse.fromUser(user));
	}

	@PostMapping("/login")
	public Response<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
		String token = userService.login(request.getName(), request.getPassword());
		return Response.success(new UserLoginResponse(token));
	}

	@GetMapping("/alarm")
	public Response<Page<AlarmResponse>> alarm(Pageable pageable, Authentication authentication) {
		User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class).orElseThrow(
			() -> new SnsApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, "Casing to User class failed"));
		return Response.success(userService.alarmList(user.getId(), pageable).map(AlarmResponse::fromAlarm));
	}

	@GetMapping("/alarm/subscribe")
	public SseEmitter subscribe(Authentication authentication) {
		User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class).orElseThrow(
			() -> new SnsApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, "Casing to User class failed"));
		return alarmService.connectAlarm(user.getId());
	}
}
