package com.tmaksimenko.storefront.model.account;

import com.tmaksimenko.storefront.model.payment.Payment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Cart {

    Double price;

    @Embedded
    Payment payment;


    @SuppressWarnings("JpaDataSourceORMInspection") // refuses to see columns
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart",
            joinColumns = {@JoinColumn(name = "account_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    @JoinColumn(name = "cart_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    Map<Long, Integer> items;

}
