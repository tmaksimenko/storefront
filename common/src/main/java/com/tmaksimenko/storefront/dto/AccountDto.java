package com.tmaksimenko.storefront.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmaksimenko.storefront.dto.order.OrderDto;
import com.tmaksimenko.storefront.model.Address;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDto {

    String username;

    String email;

    @JsonIgnore
    String password;

    Address address;

    Instant createTime;

    Instant lastModified;

    List<OrderDto> orders = new ArrayList<>();

}
