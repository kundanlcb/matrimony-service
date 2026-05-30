package com.cm.matrimony_service.biodata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
public class Address {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "biodata_id", nullable = false)
	private Biodata biodata;

	@Enumerated(EnumType.STRING)
	@Column(name = "address_type", nullable = false, length = 20)
	private AddressType addressType;

	@Column(nullable = false, length = 100)
	private String city;

	@Column(nullable = false, length = 100)
	private String state;

	@Column(nullable = false, length = 100)
	private String country;

	@Column(length = 20)
	private String pincode;

	@Column(name = "street_address", columnDefinition = "text")
	private String streetAddress;

	@Column(name = "is_primary", nullable = false)
	private boolean primary = false;

	public Address(Biodata biodata, AddressType addressType, String city, String state, String country, String pincode, String streetAddress, boolean primary) {
		this.biodata = biodata;
		this.addressType = addressType;
		this.city = city;
		this.state = state;
		this.country = country;
		this.pincode = pincode;
		this.streetAddress = streetAddress;
		this.primary = primary;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
	}
}
