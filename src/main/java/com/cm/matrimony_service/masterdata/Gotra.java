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

/**
 * Entity representing a Gotra in the master data.
 */
@Entity
@Table(name = "gotras")
@Getter
@Setter
@NoArgsConstructor
public class Gotra {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Constructs a new Gotra with the given name.
     *
     * @param name the name of the gotra
     */
    public Gotra(String name) {
        this.name = name;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
