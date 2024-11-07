package com.example.demo.service;

import com.example.demo.dto.request.UserRegisterRequest;
import com.example.demo.dto.response.FriendshipResponse;
import com.example.demo.entity.Friendship;
import com.example.demo.enums.FriendshipStatus;
import com.example.demo.handler.AppException;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FriendshipRepository friendshipRepository;

    private static final ObjectMapper mapper = new ObjectMapper();

    public User getUserFromDB(Long id){
        return userRepository.findById(id).orElseThrow(
                () -> new AppException("User with id " + id + " is not existed", HttpStatus.NOT_FOUND)
        );
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public UserResponse registerUser(UserRegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail()))
            throw new AppException("EMAIL_EXISTED");

        User user = request.toUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return new UserResponse(user);
    }

    public UserResponse getUserById(Long id){
        User user = getUserFromDB(id);
        return new UserResponse(user);
    }

    public List<UserResponse> getUsersByUsername(String username){
        List<User> users = userRepository.findByUsername(username);

        if(users.isEmpty())
            throw new AppException("User with username '" + username + "' is not found", HttpStatus.NOT_FOUND);

        return users.stream()
                .map(UserResponse::new)
                .toList();
    }

    public String changePassword(Long id, String oldPassword, String newPassword) throws JOSEException {
        User user = getUserFromDB(id);

        if(passwordEncoder.matches(oldPassword, user.getPassword())){
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordVersion(UUID.randomUUID().toString());
            userRepository.save(user);
            return jwtUtil.generateToken(id);
        }

        throw new AppException("old password is not matched", HttpStatus.NOT_ACCEPTABLE);
    }

    @Transactional
    public FriendshipResponse requestAddFriend(Long senderId, Long receiverId){
        if(Objects.equals(senderId, receiverId))
            throw new AppException("cannot add friend with yourself !", HttpStatus.BAD_REQUEST);

        var sender = getUserFromDB(senderId);
        var receiver = getUserFromDB(receiverId);

        for(Friendship friendship : sender.getSentRequest()){
            if(friendship.getId().getReceiverId().equals(receiverId))
                return new FriendshipResponse(receiver, friendship, true);
        }

        var friendship = new Friendship(sender, receiver);

        sender.getSentRequest().add(friendship);
        userRepository.save(sender);
        receiver.getReceivedRequest().add(friendship);
        userRepository.save(receiver);

        return new FriendshipResponse(receiver, friendship, true);
    }

    @Transactional
    public Object refuseAddFriendRequest(Long senderId, Long receiverId){
        var sender = getUserFromDB(senderId);
        var receiver = getUserFromDB(receiverId);

        for(Friendship friendship : sender.getSentRequest()){
            if(friendship.getId().getReceiverId().equals(receiverId)){
                sender.getSentRequest().remove(friendship);
                userRepository.save(sender);
                receiver.getReceivedRequest().remove(friendship);
                userRepository.save(receiver);

                return null;
            }
        }

        return null;
    }

    @Transactional
    public Instant agreeAddFriendRequest(Long senderId, Long receiverId){
        var sender = getUserFromDB(senderId);

        Instant instant = null;
        for(Friendship friendship : sender.getSentRequest()){
            if(friendship.getId().getReceiverId().equals(receiverId)){
                friendship.setStatus(FriendshipStatus.FRIEND);
                friendship.setUpdatedAt(Instant.now());
                instant = friendship.getUpdatedAt();
                friendshipRepository.save(friendship);
            }
        }
        return instant;
    }

    public List<FriendshipResponse> getAllFriendships(Long id){
        var user = getUserFromDB(id);
        List<FriendshipResponse> result = new ArrayList<>();

        user.getSentRequest().forEach(friendship ->
            result.add(new FriendshipResponse(friendship.getReceiver(), friendship, true))
        );

        user.getReceivedRequest().forEach(friendship ->
                result.add(new FriendshipResponse(friendship.getSender(), friendship, false))
        );

        return result;
    }

    public List<FriendshipResponse> getAccepted(Long id){
        return getAllFriendships(id).stream()
                .filter(friendshipResponse -> friendshipResponse.getStatus().equals(FriendshipStatus.FRIEND))
                .toList();
    }

    @Transactional
    public boolean deleteLeftFriendship(User leftUser, User rightUser){
        for(Friendship friendship : leftUser.getSentRequest()){
            if(friendship.getReceiver().getId().equals(rightUser.getId())){
                leftUser.getSentRequest().remove(friendship);
                userRepository.save(leftUser);
                rightUser.getReceivedRequest().remove(friendship);
                userRepository.save(rightUser);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public Object unfriend(Long userId, Long friendId){
        var user = getUserFromDB(userId);
        var friend = getUserFromDB(friendId);

        if(!deleteLeftFriendship(user, friend))
            deleteLeftFriendship(friend, user);

        return null;
    }

    public Page<UserResponse> searchUsersByPriority(String username, Long userId, Pageable pageable) {
        List<User> prioritizedUsers = userRepository.findFriendsOrPendingUsers(username, userId);

        List<User> unrelatedUsers = userRepository.findUnrelatedUsers(username, userId);

        List<User> combinedUsers = new ArrayList<>();
        combinedUsers.addAll(prioritizedUsers);
        combinedUsers.addAll(unrelatedUsers);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), combinedUsers.size());
        List<UserResponse> userResponses = combinedUsers.subList(start, end).stream()
                .map(UserResponse::new)
                .toList();

        return new PageImpl<>(userResponses, pageable, combinedUsers.size());
    }

    public Object setAvatar(Long id, String url) {
        var user = getUserFromDB(id);
        user.setAvatarUrl(url);
        userRepository.save(user);
        return null;
    }
}
