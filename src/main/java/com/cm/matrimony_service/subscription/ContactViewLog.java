package com.cm.matrimony_service.subscription;

import com.cm.matrimony_service.user.User;
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
@Table(name = "contact_view_logs",
	uniqueConstraints = @UniqueConstraint(name = "unique_view", columnNames = {"viewer_id", "viewed_user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ContactViewLog {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "viewer_id", nullable = false)
	private User viewer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "viewed_user_id", nullable = false)
	private User viewedUser;

	@Column(name = "viewed_at", nullable = false)
	private Instant viewedAt;

	public ContactViewLog(User viewer, User viewedUser) {
		this.viewer = viewer;
		this.viewedUser = viewedUser;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (viewedAt == null) {
			viewedAt = Instant.now();
		}
	}
}
