package com.example.demo.repository;

import com.example.demo.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Friendship.FriendshipId> {

    boolean existsByIdSenderIdAndIdReceiverId(Long senderId, Long receiverId);

    @Query(value = "SELECT * FROM friendships WHERE sender_id = :senderId AND receiver_id = :receiverId", nativeQuery = true)
    Optional<Friendship> findById(@RequestParam("senderId") Long senderId, @RequestParam("receiverId") Long receiverId);

}
