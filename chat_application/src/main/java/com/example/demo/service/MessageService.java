package com.example.demo.service;

import com.example.demo.dto.request.PostMessageRequest;
import com.example.demo.dto.request.SendMessageRequest;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.entity.ConversationParticipant;
import com.example.demo.entity.Message;
import com.example.demo.entity.MessageStatus;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.handler.AppException;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public Message getMessageFromDB(Long id){
        return messageRepository.findById(id).orElseThrow(
                () -> new AppException("Message is not existed", HttpStatus.NOT_FOUND)
        );
    }

    public MessageResponse getLastMessage(Long userId, Long conversationId){
        var conversation = conversationService.getConversationFromDB(conversationId);

        return new MessageResponse(conversation.getMessages().getLast(), userId);
    }

    public List<MessageResponse> getAllMessage(Long userId, Long conversationId){
        var conversation = conversationService.getConversationFromDB(conversationId);

        return conversation.getMessages()
                .stream()
                .map(message -> new MessageResponse(message, userId))
                .toList();
    }

    @Transactional
    public MessageResponse postMessage(Long userId, PostMessageRequest request){
        var conversation = conversationService.getConversationFromDB(request.getConversationId());

        User sender = userService.getUserFromDB(userId);

        var message = new Message();
        message.setUser(sender);
        message.setConversation(conversation);

        message.setContent(request.getContent());
        message.setImageUrl(request.getImageUrl());

        messageRepository.save(message);

        var senderMessageStatus = new MessageStatus(sender, message);
        senderMessageStatus.setStatus(Status.SENT);
        message.getMessageStatusList().add(senderMessageStatus);
        sender.getMessageStatusList().add(senderMessageStatus);
        userRepository.save(sender);
        messageRepository.save(message);

        for(ConversationParticipant participant : conversation.getParticipants()){
            if(participant.getId().getUserId().equals(userId))
                continue;

            var receiver = participant.getUser();
            var receiverMessageStatus = new MessageStatus(receiver, message);
            receiverMessageStatus.setStatus(Status.DELIVERED);
            message.getMessageStatusList().add(receiverMessageStatus);
            receiver.getMessageStatusList().add(receiverMessageStatus);
            userRepository.save(receiver);
            messageRepository.save(message);
        }

        return new MessageResponse(message, Status.SENT);
    }

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) throws IOException {
        var userId = request.getSenderId();
        var message = getMessageFromDB(request.getMessageId());
        var response = new MessageResponse(message, Status.DELIVERED);
        if(userId.equals(request.getSenderId()))
            response.setStatus(Status.SENT);

        return response;
    }

    @Transactional
    public Object updateMessageStatus(Long messageId, Long userId) throws IOException {
        var message = getMessageFromDB(messageId);
        for(MessageStatus messageStatus : message.getMessageStatusList()){
            if(messageStatus.getId().getUserId().equals(userId)){
                var user = messageStatus.getUser();
                if(messageStatus.getStatus().equals(Status.DELIVERED)){
                    messageStatus.setStatus(Status.READ);
                    messageStatus.setUpdatedAt(Instant.now());
                    userRepository.save(user);
                    messageRepository.save(message);
                }
                return null;
            }
        }

        throw new AppException("Not found", HttpStatus.NOT_FOUND);
    }

}
