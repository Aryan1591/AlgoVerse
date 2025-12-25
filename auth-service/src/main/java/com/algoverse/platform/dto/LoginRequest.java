package com.algoverse.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    @Email 
    @NotBlank(message = "Email cannot be blank") 
    String email,
    
    @NotBlank(message = "Password cannot be blank") 
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_=+-]).{8,16}", message = "Password should contains At least one lowercase letter,one uppercase letter,one numeric value and one special symbol ")
    String password
) {}
