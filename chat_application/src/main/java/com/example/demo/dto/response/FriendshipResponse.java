package com.example.demo.dto.response;

import com.example.demo.entity.Friendship;
import com.example.demo.entity.User;
import com.example.demo.enums.FriendshipStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class FriendshipResponse {

    private Long userId;
    private String username;
    private Boolean isFromMe;
    private String avatarUrl;
    private Instant updatedAt;
    private FriendshipStatus status;

    public FriendshipResponse(User user, Friendship friendship, Boolean isFromMe){
        this.userId = user.getId();
        this.username = user.getUsername();
        this.isFromMe = isFromMe;
        this.avatarUrl = user.getAvatarUrl();
        this.updatedAt = friendship.getUpdatedAt();
        this.status = friendship.getStatus();
    }

}
