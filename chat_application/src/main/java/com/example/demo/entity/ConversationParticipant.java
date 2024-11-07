package com.example.demo.entity;

import com.example.demo.enums.ParticipantRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conversation_participants")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConversationParticipant {

    @EmbeddedId
    private ConversationParticipantId id;

    @Enumerated(EnumType.STRING)
    private ParticipantRole role = ParticipantRole.MEMBER;

    @JsonIgnore
    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(optional = false)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Setter
    @Getter
    @Embeddable
    public static class ConversationParticipantId {
        @Column(name = "conversation_id")
        private Long conversationId;

        @Column(name = "user_id")
        private Long userId;
    }

    public ConversationParticipant(User user, Conversation conversation){
        this.user = user;
        this.conversation = conversation;
        this.id = new ConversationParticipantId();
        this.id.setUserId(user.getId());
        this.id.setConversationId(conversation.getId());
    }

}
