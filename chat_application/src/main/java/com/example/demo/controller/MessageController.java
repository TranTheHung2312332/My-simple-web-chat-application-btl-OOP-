package com.example.demo.controller;

import com.example.demo.dto.request.PostMessageRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.MessageService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/last")
    public ApiResponse getLastMessage(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam Long conversationId
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(messageService.getLastMessage(id, conversationId));
    }

    @GetMapping("/all")
    public ApiResponse getAllMessages(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam Long conversationId
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(messageService.getAllMessage(id, conversationId));
    }

    @PostMapping("/post")
    public ApiResponse postMessage(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody PostMessageRequest request
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(messageService.postMessage(id, request));
    }

    @PutMapping("update-status")
    public ApiResponse updateStatus(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                    @RequestParam Long messageId
    ) throws ParseException, IOException {
        String token = authorizationHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(messageService.updateMessageStatus(messageId, userId));
    }

}
