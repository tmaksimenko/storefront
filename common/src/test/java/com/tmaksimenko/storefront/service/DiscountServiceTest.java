package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.base.Audit;
import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.repository.GeneralDiscountRepository;
import com.tmaksimenko.storefront.repository.ProductDiscountRepository;
import com.tmaksimenko.storefront.service.discount.DiscountService;
import com.tmaksimenko.storefront.service.discount.DiscountServiceImplementation;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ExtendWith(MockitoExtension.class)
public class DiscountServiceTest {

    @Mock
    GeneralDiscountRepository generalDiscountRepository;

    @Mock
    ProductDiscountRepository productDiscountRepository;

    DiscountService discountService;

    GeneralDiscount generalDiscount;

    ProductDiscount productDiscount;

    @BeforeEach
    public void setup () {
        discountService = new DiscountServiceImplementation(generalDiscountRepository, productDiscountRepository);
        generalDiscount = GeneralDiscount.builder()
                .id(1L)
                .audit(new Audit("Test"))
                .percent(10.0)
                .role(Role.ROLE_STAFF)
                .build();
        productDiscount = ProductDiscount.builder()
                .id(2L)
                .audit(new Audit("Test"))
                .percent(15.0)
                .product(Product.builder()
                        .id(1000L)
                        .name("testName")
                        .brand("testBrand").price(10.0).weight(1.0).build())
                .build();
    }

    @Test
    @DisplayName("Successful findAllDiscounts")
    public void test_successful_findAllDiscounts () {
        // given
        given(generalDiscountRepository.findAll()).willReturn(List.of(generalDiscount));
        given(productDiscountRepository.findAll()).willReturn(List.of(productDiscount));

        // when
        List<? super Discount> discounts = discountService.findAllDiscounts();

        // then
        assertThat(discounts).contains(generalDiscount).contains(productDiscount);
    }

    @Test
    @DisplayName("Empty findAllDiscounts")
    public void test_failed_findAllDiscounts () {
        // given
        given(generalDiscountRepository.findAll()).willReturn(List.of());
        given(productDiscountRepository.findAll()).willReturn(List.of());

        // when
        List<? super Discount> discounts = discountService.findAllDiscounts();

        // then
        assertThat(discounts).isEmpty();
    }

    @Test
    @DisplayName("Successful findById - generalDiscount")
    public void test_successful_findById_generalDiscount () {
        // given
        given(generalDiscountRepository.findById(generalDiscount.getId())).willReturn(Optional.of(generalDiscount));

        // when
        Optional<? extends Discount> discount = discountService.findById(generalDiscount.getId());

        // then
        assertThat(discount).isPresent().get().isSameAs(generalDiscount);
    }

    @Test
    @DisplayName("Successful findById - productDiscount")
    public void test_successful_findById_productDiscount () {
        // given
        given(generalDiscountRepository.findById(productDiscount.getId())).willReturn(Optional.empty());
        given(productDiscountRepository.findById(productDiscount.getId())).willReturn(Optional.of(productDiscount));

        // when
        Optional<? extends Discount> discount = discountService.findById(productDiscount.getId());

        // then
        assertThat(discount).isPresent().get().isSameAs(productDiscount);
    }

    @Test
    @DisplayName("Failed findById - not found")
    public void test_failed_findById () {
        Long badId = 3L;
        // given
        given(generalDiscountRepository.findById(badId)).willReturn(Optional.empty());
        given(productDiscountRepository.findById(badId)).willReturn(Optional.empty());

        // when
        Optional<? extends Discount> discount = discountService.findById(badId);

        // then
        assertThat(discount).isEmpty();
    }


}
