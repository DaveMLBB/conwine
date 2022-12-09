package com.conwine.server.controllers;

import com.conwine.server.models.User;
import com.conwine.server.notification.MailNotificationService;
import com.conwine.server.payload.request.LoginRequest;
import com.conwine.server.payload.request.PasswordResetRequest;
import com.conwine.server.payload.request.SignupActivationDto;
import com.conwine.server.payload.request.SignupRequest;
import com.conwine.server.payload.response.JwtResponse;
import com.conwine.server.repositories.UserRepository;
import com.conwine.server.security.jwt.JwtUtils;
import com.conwine.server.security.services.UserActivationImpl;
import com.conwine.server.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired
    private UserActivationImpl activation;



    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> signin(@Valid @RequestBody LoginRequest loginRequest){

        User userControl = userRepository.findByUsername(loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));

        if (!userControl.isActive()){
           throw new RuntimeException("user not active");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(user->user.getAuthority())
                .collect(Collectors.toList());


        JwtResponse jwtResponse = new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                roles);

        return new ResponseEntity<JwtResponse>(jwtResponse, HttpStatus.OK);
    }





    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) throws Exception {
        User userMail = new User();
        userMail.setEmail(signupRequest.getEmail());
        if (userRepository.findByEmail(userMail.getEmail()) != null) {
            return ResponseEntity
                    .badRequest()
                    .body("Email is already in use");
        }

        @Valid User user = new User(
                signupRequest.getFirstName(),
                signupRequest.getLastName(),
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword()));

        user.setActivationCode(UUID.randomUUID().toString());
        mailNotificationService.sendActivationEmail(user);

        userRepository.save(user);
        return ResponseEntity
                .ok()
                .body("Utente creato");
    }





    @PostMapping("/activation")
    public ResponseEntity signup(@RequestBody SignupActivationDto signupActivationDto)throws Exception{

        activation.activate(signupActivationDto);

        return ResponseEntity
                .ok()
                .body("Utente validato");
    }

    @PostMapping("/reset-password")
    public ResponseEntity signup(@RequestBody PasswordResetRequest passwordResetRequest)throws Exception{

        User userControl = userRepository.findByEmail(passwordResetRequest.getEmail());
        if (!userControl.isActive()){
            throw new RuntimeException("user not active");
        }

        if (!userControl.getUsername().equals(passwordResetRequest.getUsername())){
            throw new RuntimeException("user not active");
        }

        if (!userControl.getFirstName().equals(passwordResetRequest.getFirstName())){
            throw new RuntimeException("user not active");
        }

        if (!userControl.getLastName().equals(passwordResetRequest.getLastName())){
            throw new RuntimeException("user not active");
        }
        userControl.setPassword(encoder.encode(passwordResetRequest.getNewPasword()));

        userRepository.save(userControl);
        return ResponseEntity
                .ok()
                .body("Password cambiata");
    }
}
