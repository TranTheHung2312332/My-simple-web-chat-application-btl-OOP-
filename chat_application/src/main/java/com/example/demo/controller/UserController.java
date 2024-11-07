package com.example.demo.controller;

import com.example.demo.dto.request.ChangePasswordRequest;
import com.example.demo.dto.request.UserLoginRequest;
import com.example.demo.dto.request.UserRegisterRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.handler.AppException;
import com.example.demo.utils.JwtUtil;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/users")
    public ApiResponse getAllUsers(){
        return new ApiResponse(userService.getAllUsers());
    }

    @GetMapping("/users/search")
    public ApiResponse searchUsers(
            @RequestParam String username,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            Pageable pageable) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(userService.searchUsersByPriority(username, id, pageable));
    }

    @GetMapping("/users/me")
    public ApiResponse getInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(new UserResponse(userService.getUserFromDB(id)));
    }

    @PostMapping("/public/register")
    public ApiResponse registerUser(@Valid @RequestBody UserRegisterRequest request){
        return new ApiResponse(userService.registerUser(request));
    }

    @PostMapping("/public/login")
    public ApiResponse loginByEmailPassword(@RequestBody UserLoginRequest request){
        return new ApiResponse(authenticationService.loginByEmailPassword(request));
    }

    @PutMapping("/change-password")
    public ApiResponse changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader)
            throws ParseException, JOSEException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(userService.changePassword(id, request.getOldPassword(), request.getNewPassword()));
    }

    @PostMapping("/set-avatar")
    public ApiResponse setAvatar(
            @RequestParam("url") String url,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader)
            throws ParseException {
        String token = authorizationHeader.substring(7);
        Long id = jwtUtil.getUserIdFromToken(token);
        return new ApiResponse(userService.setAvatar(id, url));
    }

    @GetMapping("/ping")
    public String pong(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) throws ParseException, JOSEException {
        String token = authorizationHeader.substring(7);
        if(!jwtUtil.validateToken(token))
            throw new AppException("", HttpStatus.FORBIDDEN);
        return "pong";
    }
}
