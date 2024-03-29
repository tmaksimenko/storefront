package com.tmaksimenko.storefront.model.payment;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpiryDate {
    Integer month;
    Integer year;
    public String toString() {
        return String.format("%d/%d", this.month, this.year);
    }
}
