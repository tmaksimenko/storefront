package com.tmaksimenko.storefront.model.account;

import com.tmaksimenko.storefront.model.payment.Payment;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Cart {

    Double price;

    @Embedded
    Payment payment;


    @SuppressWarnings("JpaDataSourceORMInspection") // refuses to see columns
    @ElementCollection
    @CollectionTable(name = "cart",
            joinColumns = {@JoinColumn(name = "account_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    Map<Long, Integer> items;

}
