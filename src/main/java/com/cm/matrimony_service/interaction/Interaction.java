package com.cm.matrimony_service.interaction;

import com.cm.matrimony_service.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "interactions",
	uniqueConstraints = @UniqueConstraint(name = "unique_interaction", columnNames = {"from_user_id", "to_user_id", "type"}),
	indexes = @Index(name = "idx_interactions_to_user_type", columnList = "to_user_id,type"))
@Getter
@Setter
@NoArgsConstructor
public class Interaction {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "from_user_id", nullable = false)
	private User fromUser;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "to_user_id", nullable = false)
	private User toUser;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private InteractionType type;

	@Column(nullable = false)
	private Instant timestamp;

	public Interaction(User fromUser, User toUser, InteractionType type) {
		this.fromUser = fromUser;
		this.toUser = toUser;
		this.type = type;
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
