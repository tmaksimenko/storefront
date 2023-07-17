package com.tmaksimenko.storefront.tokentest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class AuthRequest {

    @Schema(defaultValue = "admin", type = "string", description = "User Login")
    @NotBlank
    private String emailOrLogin;

    @Schema(defaultValue = "password", type = "string", description = "User password")
    @NotBlank
    private String password;

}
