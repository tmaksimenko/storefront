package com.tmaksimenko.storefront.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmaksimenko.storefront.dto.product.ProductIdWithQuantity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Data
@EqualsAndHashCode(callSuper=false)
@FieldDefaults(level = PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderCreateDto {

    Long id;

    String username;

    List<ProductIdWithQuantity> productIdsWithQuantities;

//    List<Long> productIds;
//
//    List<Integer> productQuantities;
//
//    public OrderCreateDto (Long id, String username, List<Long> productIds) {
//        this.id = id;
//        this.username = username;
//        this.productIds = productIds;
//    }

}

