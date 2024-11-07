package com.example.demo.entity;

import com.example.demo.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "message_status")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageStatus {

    @EmbeddedId
    private MessageStatusId id;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant updatedAt = Instant.now();

    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    private Message message;

    @Getter
    @Setter
    @Embeddable
    public static class MessageStatusId {
        @Column(name = "user_id")
        private Long userId;

        @Column(name = "message_id")
        private Long messageId;
    }

    public MessageStatus(User user, Message message){
        this.user = user;
        this.message = message;
        this.id = new MessageStatusId();
        this.id.setUserId(user.getId());
        this.id.setMessageId(message.getId());
    }

}
