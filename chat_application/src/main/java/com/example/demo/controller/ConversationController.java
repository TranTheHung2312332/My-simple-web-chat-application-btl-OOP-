package com.example.demo.controller;

import com.example.demo.dto.request.CreateGroupRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.ConversationService;
import com.example.demo.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ConversationService conversationService;

    @GetMapping("/all")
    public ApiResponse getAllConversations(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(conversationService.getConversationsByUserId(userId));
    }

    @GetMapping("/get")
    public ApiResponse getOneToOneConversation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam("userId") Long partnerId
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(conversationService.getOneToOneConversationByUserId(userId, partnerId));
    }

    @GetMapping("/search")
    public ApiResponse getConversationsByName(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam("name") String name
    ) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(conversationService.getConversationsByName(id, name));
    }

    @PostMapping("/create")
    public ApiResponse createPairConversation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam("partnerId") Long partnerId)
    throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(conversationService.createPairConversation(id, partnerId));
    }

    @PostMapping("/create-group")
    public ApiResponse createGroupConversation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody @Valid CreateGroupRequest request)
    throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(conversationService.createGroupConversation(id, request));
    }

    @DeleteMapping("/kick")
    public ApiResponse kickMember(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                  @RequestParam("memberId") Long memberId,
                                  @RequestParam("conversationId") Long conversationId)
    throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(conversationService.kickMember(id, memberId, conversationId));
    }

    @PostMapping("/add")
    public ApiResponse addMember(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                  @RequestParam("memberId") Long memberId,
                                  @RequestParam("conversationId") Long conversationId)
    throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(conversationService.addMemberToGroup(id, memberId, conversationId));
    }

    @PostMapping("/set-avatar")
    public ApiResponse setAvatar(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                 @RequestParam("conversationId") Long conversationId,
                                 @RequestParam("url") String url)
            throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(conversationService.setAvatar(id, conversationId, url));
    }

}
