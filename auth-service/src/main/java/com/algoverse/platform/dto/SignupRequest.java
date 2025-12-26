package com.algoverse.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Digits;
import java.util.List;

import com.algoverse.platform.entity.Qualification;


public record SignupRequest(
    

    @NotBlank 
    @Email(message = "Please provide a valid email address", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    String email,
    
    @NotBlank
    @Size(min = 8, message = "Password must be atleast 8 and max of 16 characters long")
   // @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_=+-]).{8}", message = "Password must contain uppercase, lowercase, numeric, and special characters." )
    String password,

    
    @NotBlank
    @Size(min = 12, message = "Message must be minimum of 12 digits including country code")
    @Digits(message = "Number should contain max of 15 digits.", fraction = 0, integer = 15)
    String phoneNumber,
    
    @NotBlank(message = "Please provide a username")
    String username,

    @NotNull(message = "Please provide your highest education qualification")
    Qualification educationQualification,

    @NotBlank(message = "Please provide your college name")
    String CollegeName,
    
    List<String> socialLinks
) {}
