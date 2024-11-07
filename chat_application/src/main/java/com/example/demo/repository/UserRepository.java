package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE username LIKE %:username%", nativeQuery = true)
    List<User> findByUsername(@Param("username") String username);

    @Query("""
        SELECT u 
        FROM User u
        JOIN Friendship f ON (f.sender.id = u.id OR f.receiver.id = u.id)
        WHERE (f.sender.id = :userId OR f.receiver.id = :userId) 
          AND u.id != :userId
          AND u.username LIKE %:username%
          AND f.status IN ('FRIEND', 'PENDING')
        ORDER BY 
            CASE WHEN f.status = 'FRIEND' THEN 1 
                 WHEN f.status = 'PENDING' THEN 2 
            END
        """)
    List<User> findFriendsOrPendingUsers(@Param("username") String username, @Param("userId") Long userId);

    // Lấy các User không có quan hệ nào với user hiện tại
    @Query("""
        SELECT u 
        FROM User u
        WHERE u.id != :userId
          AND u.username LIKE %:username%
          AND u.id NOT IN (
              SELECT CASE WHEN f.sender.id = :userId THEN f.receiver.id ELSE f.sender.id END
              FROM Friendship f 
              WHERE f.sender.id = :userId OR f.receiver.id = :userId
          )
        """)
    List<User> findUnrelatedUsers(@Param("username") String username, @Param("userId") Long userId);

}
