package com.tmaksimenko.storefront.service.discount;

import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.repository.GeneralDiscountRepository;
import com.tmaksimenko.storefront.repository.ProductDiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DiscountServiceImpl implements DiscountService{

    final GeneralDiscountRepository generalDiscountRepository;
    final ProductDiscountRepository productDiscountRepository;

    @Override
    public List<? super Discount> findAllDiscounts () {
        List<? super Discount> discounts = new ArrayList<>();
        discounts.addAll(generalDiscountRepository.findAll());
        discounts.addAll(productDiscountRepository.findAll());
        return discounts;
    }
}
