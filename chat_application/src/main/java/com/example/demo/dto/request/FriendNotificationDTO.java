package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendNotificationDTO {

    private Long fromId;
    private String fromName;
    private String type;
    private Object bonus;
    private String avatarUrl;

}
