package com.conwine.server.security.services;


import com.conwine.server.models.Role;
import com.conwine.server.models.User;
import com.conwine.server.payload.request.SignupActivationDto;
import com.conwine.server.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserActivationImpl {

    @Autowired
    private UserRepository userRepository;

    public void activate(SignupActivationDto signupActivationDto) throws RuntimeException{
        User user = userRepository.findByActivationCode(signupActivationDto.getActivationCode());
        if (user == null) throw new RuntimeException("user not found");
        user.setActive(true);
        user.setActivationCode(null);
        Role userRole = Role.ROLE_BUYERS;
        if (userRole == null) new RuntimeException("Initialize Role Faillure");
        user.setRole(userRole);
        userRepository.save(user);
    }
}
