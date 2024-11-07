package com.example.demo.dto.response;

import com.example.demo.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class LoginResponse {

    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private Instant createdAt;
    private String token;

    public LoginResponse(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.avatarUrl = user.getAvatarUrl();
        this.createdAt = user.getCreatedAt();
    }
}