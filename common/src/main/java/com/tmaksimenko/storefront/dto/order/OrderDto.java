package com.tmaksimenko.storefront.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmaksimenko.storefront.dto.OrderProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {

    Long id;

    String username;

    Instant createTime;

    Instant updateTime;

    List<OrderProductDto> items;
}
