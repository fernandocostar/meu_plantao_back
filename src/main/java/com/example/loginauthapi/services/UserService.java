package com.example.loginauthapi.services;

import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.repositories.ShiftRepository;
import com.example.loginauthapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

}
