package com.tmaksimenko.storefront.model.Payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpiryDate {
    int month;
    int year;
    public String toString() {
        return String.format("%d/%d", this.month, this.year);
    }
}
