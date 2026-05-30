package com.cm.matrimony_service.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blocks",
	uniqueConstraints = @UniqueConstraint(name = "unique_block", columnNames = {"from_user_id", "to_user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Block {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "from_user_id", nullable = false)
	private User fromUser;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "to_user_id", nullable = false)
	private User toUser;

	@Column(nullable = false)
	private Instant timestamp;

	public Block(User fromUser, User toUser) {
		this.fromUser = fromUser;
		this.toUser = toUser;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (timestamp == null) {
			timestamp = Instant.now();
		}
	}
}
