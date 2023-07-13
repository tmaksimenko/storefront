package com.tmaksimenko.storefront.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    @GetMapping("/process")
    public String paymentPage () {
        return "payment_page";
    }
}
