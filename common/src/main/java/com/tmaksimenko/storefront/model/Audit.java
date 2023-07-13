package com.tmaksimenko.storefront.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails)
            createdBy = ((UserDetails)principal).getUsername();
        else
            createdBy = principal.toString();
    }

    @PreUpdate
    public void preUpdate() {
        updatedOn = LocalDateTime.now();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails)
            updatedBy = ((UserDetails)principal).getUsername();
        else
            updatedBy = principal.toString();
    }

}
