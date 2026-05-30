package com.cm.matrimony_service.masterdata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "castes")
@Getter
@Setter
@NoArgsConstructor
public class Caste {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    public Caste(String name) {
        this.name = name;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
