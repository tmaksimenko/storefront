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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DiscountServiceImpl implements DiscountService{

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
    public ResponseEntity<String> createDiscount (GeneralDiscount discount) {
        generalDiscountRepository.save(discount);
        return new ResponseEntity<>("DISCOUNT CREATED", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> createDiscount (ProductDiscount discount) {
        productDiscountRepository.save(discount);
        return new ResponseEntity<>("DISCOUNT CREATED", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteDiscount (Long id) {
        try {
            generalDiscountRepository.deleteById(id);
        } catch (Exception e) { try {
                productDiscountRepository.deleteById(id);
            } catch (Exception f) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DISCOUNT NOT FOUND", new DiscountNotFoundException());
            }
        }
        return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
    }

}
