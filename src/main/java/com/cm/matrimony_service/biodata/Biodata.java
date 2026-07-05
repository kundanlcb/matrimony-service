package com.cm.matrimony_service.biodata;

import com.cm.matrimony_service.common.persistence.StringListJsonConverter;
import com.cm.matrimony_service.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the biodata profile of a user.
 */
@Entity
@Table(name = "biodata")
@Getter
@Setter
@NoArgsConstructor
public class Biodata {

	@Id
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(name = "full_name", length = 100)
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private Gender gender;

	@Min(18)
	@Max(70)
	private Integer age;

	@Column(length = 50)
	private String gotra;

	@Column(length = 50)
	private String mool;

	@Column(name = "date_of_birth", length = 30)
	private String dateOfBirth;

	@Column(name = "birth_time", length = 30)
	private String birthTime;

	@Column(name = "birth_place", length = 100)
	private String birthPlace;

	@Column(name = "father_name", length = 100)
	private String fatherName;

	@Column(name = "mother_name", length = 100)
	private String motherName;

	@Column(name = "siblings_detail", columnDefinition = "text")
	private String siblingsDetail;

	@Column(name = "grandparent_name", length = 100)
	private String grandparentName;

	@Column(length = 50)
	private String religion;

	@Column(length = 100)
	private String caste;

	@Column(length = 100)
	private String profession;

	@Column(name = "annual_income")
	private Long annualIncome;

	@Column(length = 100)
	private String location;

	private String education;

	@Column(name = "about_me", columnDefinition = "text")
	private String aboutMe;

	@Column(name = "photo_url", length = 512)
	private String photoUrl;

	@Column(length = 20)
	private String height;

	@Column(name = "marital_status", length = 30)
	private String maritalStatus;

	@Column(length = 30)
	private String diet;

	@Column(length = 30)
	private String complexion;

	@Convert(converter = StringListJsonConverter.class)
	@Column(columnDefinition = "text")
	private List<String> interests = new ArrayList<>();

	@Convert(converter = StringListJsonConverter.class)
	@Column(name = "additional_photos", columnDefinition = "text")
	private List<String> additionalPhotos = new ArrayList<>();

	@Column(name = "phone_number", length = 20)
	private String phoneNumber;

	@OneToMany(mappedBy = "biodata", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Address> addresses = new ArrayList<>();

	/**
	 * Constructs a Biodata with the given user.
	 *
	 * @param user the user this biodata is associated with
	 */
	public Biodata(User user) {
		this.user = user;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
	}
}
