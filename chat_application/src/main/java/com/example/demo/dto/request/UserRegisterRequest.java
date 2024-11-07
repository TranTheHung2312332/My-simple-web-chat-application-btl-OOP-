package com.example.demo.dto.request;

import com.example.demo.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 8, message = "username was too short")
    @Pattern(regexp = "^[a-zA-Z0-9_ ]*$", message = "Invalid characters")
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = "This field must be email")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must have at least 8 characters")
    private String password;

    public User toUser(){
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

}
