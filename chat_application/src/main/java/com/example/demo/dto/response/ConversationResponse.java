package com.example.demo.dto.response;

import com.example.demo.entity.Conversation;
import com.example.demo.entity.ConversationParticipant;
import com.example.demo.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private Long id;
    private String name;
    private ConversationType conversationType;
    private Instant created_at;
    private Instant updated_at;
    private String avatarUrl;
    private List<UserResponse> participants;

    public ConversationResponse(Conversation conversation){
        this.id = conversation.getId();
        this.name = conversation.getName();
        this.conversationType = conversation.getConversationType();
        this.created_at = conversation.getCreatedAt();
        this.avatarUrl = conversation.getAvatarUrl();
        this.participants = conversation.getParticipants()
                .stream()
                .map(ConversationParticipant::getUser)
                .map(UserResponse::new)
                .toList();
    }

}
