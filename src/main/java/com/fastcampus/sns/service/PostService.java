package com.fastcampus.sns.service;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastcampus.sns.exception.ErrorCode;
import com.fastcampus.sns.exception.SnsApplicationException;
import com.fastcampus.sns.model.AlarmArgs;
import com.fastcampus.sns.model.AlarmType;
import com.fastcampus.sns.model.Comment;
import com.fastcampus.sns.model.Post;
import com.fastcampus.sns.model.entity.AlarmEntity;
import com.fastcampus.sns.model.entity.CommentEntity;
import com.fastcampus.sns.model.entity.LikeEntity;
import com.fastcampus.sns.model.entity.PostEntity;
import com.fastcampus.sns.model.entity.UserEntity;
import com.fastcampus.sns.repository.AlarmEntityRepository;
import com.fastcampus.sns.repository.CommentEntityRepository;
import com.fastcampus.sns.repository.LikeEntityRepository;
import com.fastcampus.sns.repository.PostEntityRepository;
import com.fastcampus.sns.repository.UserEntityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostEntityRepository postEntityRepository;
	private final UserEntityRepository userEntityRepository;
	private final LikeEntityRepository likeEntityRepository;
	private final CommentEntityRepository commentEntityRepository;
	private final AlarmEntityRepository alarmEntityRepository;
	private final AlarmService alarmService;

	public Page<Post> list(Pageable pageable) {
		return postEntityRepository.findAll(pageable).map(Post::fromEntity);
	}

	public Page<Post> my(String username, Pageable pageable) {
		UserEntity userEntity = getUserEntityOrException(username);
		return postEntityRepository.findAllByUser(userEntity, pageable).map(Post::fromEntity);
	}

	@Transactional
	public void create(String title, String body, String username) {
		UserEntity userEntity = getUserEntityOrException(username);
		postEntityRepository.save(PostEntity.of(title, body, userEntity));
	}

	@Transactional
	public Post modify(String title, String body, String username, Integer postId) {
		UserEntity userEntity = getUserEntityOrException(username);
		PostEntity postEntity = getPostEntityOrException(postId);

		// post permisson
		if (postEntity.getUser() != userEntity) {
			throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION,
				String.format("%s has no permission with %s", username, postId));
		}

		postEntity.setTitle(title);
		postEntity.setBody(body);

		return Post.fromEntity(postEntityRepository.saveAndFlush(postEntity));
	}

	@Transactional
	public void delete(String username, Integer postId) {
		UserEntity userEntity = getUserEntityOrException(username);
		PostEntity postEntity = getPostEntityOrException(postId);

		if (postEntity.getUser() != userEntity) {
			throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION,
				String.format("%s has no permission with %s", username, postId));
		}

		commentEntityRepository.deleteAllByPost(postEntity);
		likeEntityRepository.deleteAllByPost(postEntity);
		postEntityRepository.delete(postEntity);
	}

	@Transactional
	public void like(Integer postId, String username) {
		UserEntity userEntity = getUserEntityOrException(username);
		PostEntity postEntity = getPostEntityOrException(postId);

		likeEntityRepository.findByUserAndPost(userEntity, postEntity).ifPresent(it -> {
			throw new SnsApplicationException(ErrorCode.ALREADY_LIKED,
				String.format("%s already liked the %d", username, postId));
		});

		likeEntityRepository.save(LikeEntity.of(userEntity, postEntity));

		AlarmEntity alarmEntity = alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_LIKE_ON_POST,
			new AlarmArgs(userEntity.getId(), postId)));
		alarmService.send(alarmEntity.getId(), postEntity.getUser().getId());
	}


	public long likeCount(Integer postId) {
		PostEntity postEntity = getPostEntityOrException(postId);
		return likeEntityRepository.countByPost(postEntity);
	}

	@Transactional
	public void comment(Integer postId, String username, String comment) {
		PostEntity postEntity = getPostEntityOrException(postId);
		UserEntity userEntity = getUserEntityOrException(username);

		commentEntityRepository.save(CommentEntity.of(userEntity, postEntity, comment));

		AlarmEntity alarmEntity = alarmEntityRepository.save(
			AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_COMMENT_ON_POST,
				new AlarmArgs(userEntity.getId(), postId)));
		alarmService.send(alarmEntity.getId(), postEntity.getUser().getId());
	}

	public Page<Comment> getComment(Integer postId, Pageable pageable) {
		PostEntity postEntity = getPostEntityOrException(postId);
		return commentEntityRepository.findAllByPost(postEntity, pageable).map(Comment::fromEntity);
	}

	private PostEntity getPostEntityOrException(Integer postId) {
		return postEntityRepository.findById(postId).orElseThrow(
			() -> new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("%s not founded", postId)));
	}

	private UserEntity getUserEntityOrException(String username) {
		return userEntityRepository.findByUserName(username).orElseThrow(
			() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", username)));
	}
}
