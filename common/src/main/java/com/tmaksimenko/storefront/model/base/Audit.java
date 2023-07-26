package com.tmaksimenko.storefront.model.base;

import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Audit {
    LocalDateTime createdOn;

    String createdBy;

    LocalDateTime updatedOn;

    String updatedBy;

    @PrePersist
    public void prePersist() {
        createdOn = LocalDateTime.now();
        createdBy = SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PreUpdate
    public void preUpdate() {
        updatedOn = LocalDateTime.now();
        updatedBy = SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Audit (String createdBy) {
        this.createdOn = LocalDateTime.now();
        this.createdBy = createdBy;
    }

}
