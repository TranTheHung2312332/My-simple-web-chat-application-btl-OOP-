package com.example.demo.dto.response;

import com.example.demo.entity.Message;
import com.example.demo.entity.MessageStatus;
import com.example.demo.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private Long conversationId;
    private String content;
    private Instant createdAt;
    private Status status;
    private String imageUrl;

    public MessageResponse(Message message, Long userId){
        this.id = message.getId();
        this.senderId = message.getUser().getId();
        this.senderName = message.getUser().getUsername();
        this.senderAvatarUrl = message.getUser().getAvatarUrl();
        this.conversationId = message.getConversation().getId();
        this.content = message.getContent();
        this.createdAt = message.getConversation().getCreatedAt();
        this.imageUrl = message.getImageUrl();

        for(MessageStatus messageStatus : message.getMessageStatusList()){
            if(messageStatus.getUser().getId().equals(userId)){
                this.status = messageStatus.getStatus();
                return;
            }
        }
    }

    public MessageResponse(Message message, Status status){
        this.id = message.getId();
        this.senderId = message.getUser().getId();
        this.senderName = message.getUser().getUsername();
        this.senderAvatarUrl = message.getUser().getAvatarUrl();
        this.conversationId = message.getConversation().getId();
        this.content = message.getContent();
        this.createdAt = message.getConversation().getCreatedAt();
        this.status = status;
        this.imageUrl = message.getImageUrl();
    }

}
