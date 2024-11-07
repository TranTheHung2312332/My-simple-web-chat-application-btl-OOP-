package com.example.demo.utils;


import com.example.demo.handler.AppException;
import com.example.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class JwtUtil {

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.SECRET_KEY}")
    private String SECRET_KEY;

    // 30 days
    private static final Date expirationTime = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

    public String generateToken(Long userId) throws JOSEException {
        var userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty())
            throw  new AppException("User is not existed", HttpStatus.NOT_FOUND);

        var user = userOptional.get();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userId + "")
                .issuer("chat-application")
                .issueTime(new Date())
                .expirationTime(expirationTime)
                .claim("passwordVersion", user.getPasswordVersion())
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaimsSet);
        signedJWT.sign(new MACSigner(SECRET_KEY));

        return signedJWT.serialize();
    }

    public boolean validateToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SECRET_KEY);

        if(signedJWT.verify(verifier)){
            Long userId = getUserIdFromToken(token);
            var userOptional = userRepository.findById(userId);
            if(userOptional.isEmpty())
                throw new AppException("User is not existed", HttpStatus.NOT_FOUND);

            var user = userOptional.get();
            if(!user.getPasswordVersion().equals(signedJWT.getJWTClaimsSet().getClaim("passwordVersion")))
                return false;

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if(expirationTime.before(new Date()))
                return false;
            return true;
        }

        return false;
    }

    public Long getUserIdFromToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return Long.valueOf(signedJWT.getJWTClaimsSet().getSubject());
    }

}
