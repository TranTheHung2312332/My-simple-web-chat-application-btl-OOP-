package com.example.demo.controller;

import com.example.demo.dto.request.FriendNotificationDTO;
import com.example.demo.dto.request.GlobalNotificationDTO;
import com.example.demo.dto.request.SendMessageRequest;
import com.example.demo.dto.response.ConversationResponse;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.enums.SpecialMessage;
import com.example.demo.service.ConversationService;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class WebSocketController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConversationService conversationService;

    @MessageMapping("/message/{conversationId}")
    @SendTo("/chanel/{conversationId}")
    public MessageResponse sendMessage(@DestinationVariable Long conversationId, SendMessageRequest request) throws IOException {
        return messageService.sendMessage(request);
    }

    @Transactional
    @MessageMapping("/to-user/{userId}")
    @SendTo("/notification/{userId}")
    public FriendNotificationDTO sendFriendNotification(@DestinationVariable Long userId, FriendNotificationDTO request){
        request.setFromName(userService.getUserFromDB(request.getFromId()).getUsername());

        if(request.getType().equals(SpecialMessage.START_CHAT.name())){
            Long conversationId = Long.valueOf(String.valueOf(request.getBonus()));
            request.setBonus(new ConversationResponse(conversationService.getConversationFromDB(conversationId)));
        }

        return request;
    }

    @MessageMapping("/global/{userId}")
    @SendTo("/global-notification/{userId}")
    public GlobalNotificationDTO sendGlobalNotification(@DestinationVariable Long userId, GlobalNotificationDTO request){


        return request;
    }

}
