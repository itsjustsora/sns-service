package com.fastcampus.sns.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastcampus.sns.controller.request.PostCommentRequest;
import com.fastcampus.sns.controller.request.PostCreateRequest;
import com.fastcampus.sns.controller.request.PostModifyRequest;
import com.fastcampus.sns.controller.response.CommentResponse;
import com.fastcampus.sns.controller.response.PostResponse;
import com.fastcampus.sns.controller.response.Response;
import com.fastcampus.sns.model.Post;
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

	@PutMapping("/{postId}")
	public Response<PostResponse> modify(@PathVariable Integer postId, @RequestBody PostModifyRequest request, Authentication authentication) {
		Post post = postService.modify(request.getTitle(), request.getBody(), authentication.getName(), postId);
		return Response.success(PostResponse.fromPost(post));
	}

	@DeleteMapping("/{postId}")
	public Response<Void> delete(@PathVariable Integer postId, Authentication authentication) {
		postService.delete(authentication.getName(), postId);
		return Response.success();
	}

	@GetMapping
	public Response<Page<PostResponse>> list(Pageable pageable, Authentication authentication) {
		return Response.success(postService.list(pageable).map(PostResponse::fromPost));
	}

	@GetMapping("/my")
	public Response<Page<PostResponse>> my(Pageable pageable, Authentication authentication) {
		return Response.success(postService.my(authentication.getName(), pageable).map(PostResponse::fromPost));
	}

	@PostMapping("/{postId}/likes")
	public Response<Void> like(@PathVariable Integer postId, Authentication authentication) {
		postService.like(postId, authentication.getName());
		return Response.success();
	}

	@GetMapping("/{postId}/likes")
	public Response<Long> likeCount(@PathVariable Integer postId) {
		return Response.success(postService.likeCount(postId));
	}

	@PostMapping("/{postId}/comments")
	public Response<Void> comment(@PathVariable Integer postId, @RequestBody PostCommentRequest request, Authentication authentication) {
		postService.comment(postId, authentication.getName(), request.getComment());
		return Response.success();
	}

	@GetMapping("/{postId}/comments")
	public Response<Page<CommentResponse>> comment(@PathVariable Integer postId, Pageable pageable, Authentication authentication) {
		return Response.success(postService.getComment(postId, pageable).map(CommentResponse::fromComment));
	}
}
