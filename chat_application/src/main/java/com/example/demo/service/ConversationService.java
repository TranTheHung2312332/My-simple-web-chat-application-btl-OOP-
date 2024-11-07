package com.example.demo.service;

import com.example.demo.dto.request.CreateGroupRequest;
import com.example.demo.dto.response.ConversationResponse;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.ConversationParticipant;
import com.example.demo.entity.User;
import com.example.demo.enums.ConversationType;
import com.example.demo.enums.FriendshipStatus;
import com.example.demo.enums.ParticipantRole;
import com.example.demo.handler.AppException;
import com.example.demo.repository.ConversationParticipantRepository;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository conversationParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipRepository friendshipRepository;

    public Conversation getConversationFromDB(Long id){
        return conversationRepository.findById(id).orElseThrow(
                () -> new AppException("Conversation is not found", HttpStatus.NOT_FOUND)
        );
    }

    public boolean checkPartnerName(Long selfId, Conversation conversation, String name){
        if(!conversation.getConversationType().equals(ConversationType.ONE_TO_ONE))
            return false;

        var participants = conversation.getParticipants();

        var partner = participants.getLast().getUser().getId().equals(selfId) ?
                      participants.getFirst().getUser():
                      participants.getLast().getUser();

        return partner.getUsername().contains(name);
    }

    public List<ConversationResponse> getConversationsByUserId(Long userId){
        User user = userService.getUserFromDB(userId);
        var participants = user.getParticipants();

        return participants.stream()
                .map(participant ->
                        conversationRepository
                                .findById(participant.getConversation().getId())
                                .orElseThrow(() -> new AppException("Server error", HttpStatus.INTERNAL_SERVER_ERROR))
                )
                .map(ConversationResponse::new)
                .toList();
    }

    public List<ConversationResponse> getConversationsByName(Long userId, String name){
        User user = userService.getUserFromDB(userId);
        var participants = user.getParticipants();

        var result = participants.stream()
                .map(participant ->
                        conversationRepository
                                .findById(participant.getConversation().getId())
                                .orElseThrow(() -> new AppException("Server error", HttpStatus.INTERNAL_SERVER_ERROR))
                )
                .filter(conversation ->
                    conversation.getName().contains(name) ||
                    checkPartnerName(userId, conversation, name)
                )
                .map(ConversationResponse::new)
                .toList();

        if(result.isEmpty())
            throw new AppException("not found conversation " + name, HttpStatus.NOT_FOUND);
        return result;
    }

    public ConversationResponse getOneToOneConversationByUserId(Long id1, Long id2){
        var user1 = userService.getUserFromDB(id1);
        for(ConversationParticipant participant : user1.getParticipants()){
            var conversation = participant.getConversation();
            if(!conversation.getConversationType().equals(ConversationType.ONE_TO_ONE))
                continue;
            if(conversation.getParticipants().getFirst().getUser().getId().equals(id2) ||
                    conversation.getParticipants().getLast().getUser().getId().equals(id2)
            ) return new ConversationResponse(conversation);
        }

        throw new AppException("Conversation is not existed", HttpStatus.NOT_FOUND);
    }

    public boolean isFriend(Long senderId, Long receiverId){
        if(senderId.equals(receiverId))
            return false;

        var optional = friendshipRepository.findById(senderId, receiverId);
        if(optional.isPresent() && optional.get().getStatus().equals(FriendshipStatus.FRIEND))
            return true;

        optional = friendshipRepository.findById(receiverId, senderId);
        if(optional.isPresent() && optional.get().getStatus().equals(FriendshipStatus.FRIEND))
            return true;

        return false;
    }

    @Transactional
    public void addParticipant(Conversation conversation, User user, boolean admin){
        var conversationParticipant = new ConversationParticipant(user, conversation);
        if(admin) conversationParticipant.setRole(ParticipantRole.ADMIN);

        conversationParticipantRepository.save(conversationParticipant);
        user.getParticipants().add(conversationParticipant);
        conversation.getParticipants().add(conversationParticipant);
        userRepository.save(user);
        conversationRepository.save(conversation);
    }

    @Transactional
    public ConversationResponse createPairConversation(Long senderId, Long receiverId){
        User sender = userService.getUserFromDB(senderId);
        User receiver = userService.getUserFromDB(receiverId);

        if(isFriend(senderId, receiverId) || isFriend(receiverId, senderId)){
            var conversation = new Conversation();
            conversation.setName("ONE_TO_ONE" + senderId + "_" + receiverId);
            conversationRepository.save(conversation);
            addParticipant(conversation, sender, false);
            addParticipant(conversation, receiver, false);

            return new ConversationResponse(conversation);
        }

        throw new AppException("You two are not friends", HttpStatus.BAD_REQUEST);
    }

    @Transactional
    public ConversationResponse createGroupConversation(Long adminId, CreateGroupRequest request){
        User admin = userService.getUserFromDB(adminId);
        List<User> members = request.getMembers()
                .stream()
                .map(userService::getUserFromDB)
                .toList();

        if(members.stream().anyMatch(user -> !isFriend(adminId, user.getId())))
            throw new AppException("Cannot create group with one is not your friend", HttpStatus.BAD_REQUEST);

        var conversation = new Conversation();
        conversation.setConversationType(ConversationType.GROUP);
        if(request.getAvatarUrl() != null)
            conversation.setAvatarUrl(request.getAvatarUrl());

        if(request.getName() != null)
            conversation.setName(request.getName());
        else
            conversation.setName("GROUP");

        conversationRepository.save(conversation);
        addParticipant(conversation, admin, true);
        members.forEach(member -> addParticipant(conversation, member, false));

        return new ConversationResponse(conversation);
    }

    public ConversationParticipant getParticipant(User user, Conversation conversation){
        return user.getParticipants()
                .stream()
                .filter(conversationParticipant ->
                        conversationParticipant.getId().getConversationId().equals(conversation.getId())
                ).findFirst().get();
    }

    @Transactional
    public ConversationResponse kickMember(Long userId, Long memberId, Long conversationId){
        var user = userService.getUserFromDB(userId);
        var conversation = getConversationFromDB(conversationId);
        var role = getParticipant(user, conversation).getRole();

        if(conversation.getConversationType().equals(ConversationType.ONE_TO_ONE))
            throw new AppException("Invalid request", HttpStatus.BAD_REQUEST);

        if(role.equals(ParticipantRole.MEMBER))
            throw new AppException("only admin can kick participant", HttpStatus.NOT_ACCEPTABLE);

        if(role.equals(ParticipantRole.ADMIN) && userId.equals(memberId))
            throw new AppException("Group must have admin", HttpStatus.NOT_ACCEPTABLE);

        var memberUser = userService.getUserFromDB(memberId);
        var memberParticipant = getParticipant(memberUser, conversation);

        conversation.getParticipants().remove(memberParticipant);
        conversationRepository.save(conversation);

        memberUser.getParticipants().remove(memberParticipant);
        userRepository.save(memberUser);

        return new ConversationResponse(conversation);
    }

    @Transactional
    public ConversationResponse addMemberToGroup(Long userId, Long memberId, Long conversationId){
        if(!isFriend(userId, memberId))
            throw new AppException("You two are not friends", HttpStatus.BAD_REQUEST);

        var member = userService.getUserFromDB(memberId);
        var conversation = getConversationFromDB(conversationId);
        addParticipant(conversation, member, false);

        return new ConversationResponse(conversation);
    }

    public Object setAvatar(Long userId, Long conversationId, String url){
        var conversation = getConversationFromDB(conversationId);
        if(conversation.getConversationType().equals(ConversationType.ONE_TO_ONE))
            throw new AppException("You are trying set avatar of a 1:1 conversation", HttpStatus.BAD_REQUEST);

        for(ConversationParticipant participant : conversation.getParticipants()){
            if(participant.getId().getUserId().equals(userId)){
                conversation.setAvatarUrl(url);
                conversationRepository.save(conversation);
                return null;
            }
        }

        throw new AppException("only members can change group avatar", HttpStatus.NOT_ACCEPTABLE);
    }

}
