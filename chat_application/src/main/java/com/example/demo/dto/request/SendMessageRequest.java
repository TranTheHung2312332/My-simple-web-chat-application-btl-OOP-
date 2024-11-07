package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    private Long messageId;
    private Long senderId;
    private Long conversationId;
    private String content;
    private String imageUrl;

}
