package com.example.demo.entity;

import com.example.demo.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    @EmbeddedId
    private FriendshipId id;

    private Instant updatedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Getter
    @Setter
    @Embeddable
    public static class FriendshipId {
        @Column(name = "sender_id")
        private Long senderId;

        @Column(name = "receiver_id")
        private Long receiverId;
    }

    public Friendship(User sender, User receiver){
        this.id = new FriendshipId();
        this.id.setSenderId(sender.getId());
        this.id.setReceiverId(receiver.getId());
        this.sender = sender;
        this.receiver = receiver;
    }

    @ManyToOne(optional = false)
    @MapsId("senderId")
    @JoinColumn(name = "sender_id")
    public User sender;

    @ManyToOne(optional = false)
    @MapsId("senderId")
    @JoinColumn(name = "receiver_id")
    public User receiver;

}
