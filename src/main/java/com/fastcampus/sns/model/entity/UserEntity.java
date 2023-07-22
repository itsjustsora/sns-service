package com.fastcampus.sns.model.entity;

import java.sql.Timestamp;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fastcampus.sns.model.UserRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "\"user\"")
@Table
@SQLDelete(sql = "UPDATE \"user\" SET deleted_at = NOW() where id=?")
@Where(clause = "deleted_at is NULL")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String username;

	private String password;

	@Enumerated(EnumType.STRING)
	private UserRole role = UserRole.USER;

	private Timestamp registeredAt;

	private Timestamp updatedAt;

	private Timestamp deletedAt;

	@PrePersist
	void registeredAt() {
		this.registeredAt = Timestamp.from(Instant.now());
	}

	@PrePersist
	void updatedAt() {
		this.registeredAt = Timestamp.from(Instant.now());
	}

	public static UserEntity of(String username, String password) {
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername(username);
		userEntity.setPassword(password);
		return userEntity;
	}
}
