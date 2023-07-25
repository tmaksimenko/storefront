package com.tmaksimenko.storefront.service.discount;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.exception.DiscountNotFoundException;
import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.repository.GeneralDiscountRepository;
import com.tmaksimenko.storefront.repository.ProductDiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DiscountServiceImplementation implements DiscountService{

    final GeneralDiscountRepository generalDiscountRepository;
    final ProductDiscountRepository productDiscountRepository;

    @Override
    public List<Discount> findAllDiscounts () {
        List<Discount> discounts = new ArrayList<>();
        discounts.addAll(generalDiscountRepository.findAll());
        discounts.addAll(productDiscountRepository.findAll());
        return discounts;
    }

    @Override
    public Optional<? extends Discount> findById (Long id) {
        Optional<GeneralDiscount> generalDiscount = generalDiscountRepository.findById(id);
        return (generalDiscount.isPresent() ? generalDiscount : productDiscountRepository.findById(id));
    }

    @Override
    public List<GeneralDiscount> findByRole (Role role) {
        return generalDiscountRepository.findByRole(role.name());
    }

    @Override
    public Optional<ProductDiscount> findByProductId (Long id) {
        return productDiscountRepository.findByProductId(id);
    }

    @Override
    public GeneralDiscount createDiscount (GeneralDiscount discount) {
        return generalDiscountRepository.save(discount);
    }

    @Override
    public ProductDiscount createDiscount (ProductDiscount discount) {
        return productDiscountRepository.save(discount);
    }

    @Override
    public Discount deleteDiscount (Long id) {
        Optional<GeneralDiscount> generalDiscount = generalDiscountRepository.findById(id);
        if (generalDiscount.isPresent()) {
            generalDiscountRepository.delete(generalDiscount.get());
            return generalDiscount.get();
        } else {
            Optional<ProductDiscount> productDiscount = productDiscountRepository.findById(id);
            if (productDiscount.isPresent()) {
                productDiscountRepository.delete(productDiscount.get());
                return productDiscount.get();
            } else
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DISCOUNT NOT FOUND", new DiscountNotFoundException());
        }
    }

}
