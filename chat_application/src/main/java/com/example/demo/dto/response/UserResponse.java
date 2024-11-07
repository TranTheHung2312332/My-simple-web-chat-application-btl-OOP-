package com.example.demo.dto.response;

import com.example.demo.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserResponse {

    private Long id;
    private String username;
    private String avatarUrl;
    private Instant createdAt;
    private String status = null;

    public UserResponse(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.avatarUrl = user.getAvatarUrl();
        this.createdAt = user.getCreatedAt();
    }

}
