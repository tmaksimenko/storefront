package com.tmaksimenko.storefront.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class AuthRequest {

    @Schema(defaultValue = "admin", type = "string", description = "Account login")
    @NotBlank
    private String login;

    @Schema(defaultValue = "password", type = "string", description = "Account password")
    @NotBlank
    private String password;

}
