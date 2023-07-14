package com.tmaksimenko.storefront.model.Discount;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "discounts")
@SuperBuilder
@Data
@NoArgsConstructor
public class GeneralDiscount extends BaseDiscount {
}
