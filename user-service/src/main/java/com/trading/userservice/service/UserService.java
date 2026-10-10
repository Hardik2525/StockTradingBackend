package com.trading.userservice.service;

import com.trading.userservice.dto.AuthResponse;
import com.trading.userservice.dto.LoginRequest;
import com.trading.userservice.dto.RegisterRequest;
import com.trading.userservice.entity.User;
import com.trading.userservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final KafkaTemplate<String , Object> kafkaTemplate;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final String USER_REGISTERED_TOPIC = "user.registered";

    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("registering user: {}", registerRequest.getEmail());

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException(
                    "Email already registered " + registerRequest.getEmail());
        }
        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .walletBalance(registerRequest.getInitialDeposit() != null ? registerRequest.getInitialDeposit() : BigDecimal.valueOf(10000))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered : {} ", savedUser.getId());

        // Publish user.registered event to Kafka
        Map<String,Object> userRegisteredEvent = new HashMap<>();
        userRegisteredEvent.put("userId", savedUser.getId());
        userRegisteredEvent.put("email", savedUser.getEmail());
        userRegisteredEvent.put("firstName", savedUser.getFirstName());
        userRegisteredEvent.put("lastName", savedUser.getLastName());
        userRegisteredEvent.put("walletBalance",savedUser.getWalletBalance());

        kafkaTemplate.send(USER_REGISTERED_TOPIC , savedUser.getId() , userRegisteredEvent);

        String token = generateToken(savedUser.getId() , savedUser.getEmail());

        String refreshToken  = generateRefreshToken(savedUser.getId());

        return buildAuthResponse(savedUser , token , refreshToken);
    }

    public AuthResponse login(LoginRequest request){
        log.info("Login Attempt : {}",request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + request.getEmail()
                ));

        // Bcrypt Verify - compare raw password with saved password
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid Credentials");
        }
        log.info("Login successful : {}", user.getId());

        //Generate JWT token
        String token = generateToken(user.getId(),user.getEmail());
        return buildAuthResponse(user,token);
    }

    private String generateToken(String userId,String email){
        return Jwts.builder()
                .claim("userId", userId).subject(email).issuedAt(new Date()).expiration(new Date(
                        System.currentTimeMillis() + jwtExpiration
                ))
                .signWith(getSignInKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateRefreshToken(String userId){
        return Jwts.builder()
                .claim("userId", userId).subject(userId).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private AuthResponse buildAuthResponse(User user , String token,String refreshToken){
        AuthResponse response = AuthResponse.builder()
                .userId()
                .email()
                .firstName()
                .lastName()

    }
}
