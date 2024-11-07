package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/friend")
public class FriendshipController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/all")
    public ApiResponse getAllFriendships(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(userService.getAllFriendships(id));
    }

    @GetMapping("/accepted")
    public ApiResponse getAcceptedFriendship(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(userService.getAccepted(id));
    }

    @PostMapping("/add")
    public ApiResponse requestAddFriend(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam Long receiverId
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long senderId = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(userService.requestAddFriend(senderId, receiverId));
    }

    @DeleteMapping("/refuse")
    public ApiResponse refuse(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam Long senderId
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long receiverId = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(userService.refuseAddFriendRequest(senderId, receiverId));
    }

    @PutMapping("/accept")
    public ApiResponse agree(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam Long senderId
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long receiverId = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(userService.agreeAddFriendRequest(senderId, receiverId));
    }

    @DeleteMapping("/unfriend")
    public ApiResponse unfriend(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam Long friendId
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(userService.unfriend(id, friendId));
    }

}
