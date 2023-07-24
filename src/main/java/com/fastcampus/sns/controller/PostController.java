package com.fastcampus.sns.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastcampus.sns.controller.request.PostCreateRequest;
import com.fastcampus.sns.controller.response.Response;
import com.fastcampus.sns.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping
	public Response<Void> create(@RequestBody PostCreateRequest request, Authentication authentication) {
		postService.create(request.getTitle(), request.getBody(), authentication.getName());
		return Response.success();
	}
}
