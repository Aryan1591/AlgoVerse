package com.algoverse.platform.dto_security;

import com.algoverse.platform.entity_security.Qualification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record SignupRequest(
   @NotBlank
    @Email(message = "Please provide a valid email address",
           regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    String email,

    @NotBlank
    @Size(min = 8, max = 16, message = "Password must be at least 8 and max of 16 characters long")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_=+-]).{8,16}", message = "Password should contains At least one lowercase letter,one uppercase letter,one numeric value and one special symbol ")
    String password,

    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10 to 15 digits")
    String phoneNumber,

    @NotBlank(message = "Please provide a username")
    String username,

    @NotNull(message = "Please provide your highest education qualification")
    Qualification educationQualification,

    @NotBlank(message = "Please provide your college name")
    String collegeName,

    java.util.List<String> socialLinks
) {}