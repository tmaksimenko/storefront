package com.tmaksimenko.storefront.controller;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

//@RestController
//@RequestMapping("/auth")
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
