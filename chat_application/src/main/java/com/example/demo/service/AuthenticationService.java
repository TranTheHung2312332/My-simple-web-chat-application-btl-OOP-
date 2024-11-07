package com.example.demo.service;

import com.example.demo.dto.request.UserLoginRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.handler.AppException;
import com.example.demo.entity.User;
import com.example.demo.utils.JwtUtil;
import com.example.demo.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.SECRET_KEY}")
    private String SECRET_KEY;

    @Autowired
    private JwtUtil jwtUtil;

    public void setAuthentication(String token) throws ParseException, JOSEException {
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId != null) {
            var auth = new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    public LoginResponse loginByEmailPassword(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if(user == null)
            throw new AppException("email or password is incorrect", HttpStatus.NOT_FOUND);

        try{
            if(passwordEncoder.matches(request.getRawPassword(), user.getPassword())){
                var response = new LoginResponse(user);
                response.setToken(jwtUtil.generateToken(user.getId()));
                return response;
            }
        }
        catch (JOSEException e){
            throw new AppException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        throw new AppException("email or password is incorrect", HttpStatus.NOT_FOUND);
    }

}
