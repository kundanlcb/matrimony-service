package com.cm.matrimony_service.match;

import com.cm.matrimony_service.common.persistence.StringListJsonConverter;
import com.cm.matrimony_service.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the matching criteria preferences for a user.
 */
@Entity
@Table(name = "match_criteria")
@Getter
@Setter
@NoArgsConstructor
public class MatchCriteria {

	@Id
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(name = "min_age", nullable = false)
	private Integer minAge = 18;

	@Column(name = "max_age", nullable = false)
	private Integer maxAge = 70;

	@Column(name = "min_income", nullable = false)
	private Long minIncome = 0L;

	@Column(name = "marital_status", length = 30)
	private String maritalStatus;

	@Column(length = 30)
	private String diet;

	@Convert(converter = StringListJsonConverter.class)
	@Column(name = "allowed_gotras", columnDefinition = "text")
	private List<String> allowedGotras = new ArrayList<>();

	@Convert(converter = StringListJsonConverter.class)
	@Column(name = "allowed_locations", columnDefinition = "text")
	private List<String> allowedLocations = new ArrayList<>();

	@Convert(converter = StringListJsonConverter.class)
	@Column(name = "allowed_professions", columnDefinition = "text")
	private List<String> allowedProfessions = new ArrayList<>();

	/**
	 * Constructs a MatchCriteria for the specified user.
	 *
	 * @param user the user
	 */
	public MatchCriteria(User user) {
		this.user = user;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
	}
}
