package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.dto.auth.AuthLoginRequestDTO;
import com.example.loginauthapi.dto.auth.AuthRegisterRequestDTO;
import com.example.loginauthapi.dto.auth.ErrorResponseDTO;
import com.example.loginauthapi.dto.auth.ResponseDTO;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthLoginRequestDTO body) {
        try {
            User user = this.userRepository.findByEmail(body.email()).orElseThrow(() -> new RuntimeException("Invalid email or password, please try again"));
            if (passwordEncoder.matches(body.password(), user.getPassword())) {
                String token = this.tokenService.generateToken(user);
                return ResponseEntity.ok(new ResponseDTO(user.getName(), token, user.getEmail()));
            }
            throw new RuntimeException("Invalid email or password, please try again");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO("Error while trying to login. Please try again later."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody AuthRegisterRequestDTO body){
        Optional<User> user = this.userRepository.findByEmail(body.email());

        if(user.isEmpty()) {
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            newUser.setProfessionalType(body.professionalType());
            newUser.setProfessionalRegister(body.professionalRegister());
            newUser.setState(body.state());
            newUser.setCity(body.city());
            this.userRepository.save(newUser);

            String token = this.tokenService.generateToken(newUser);
            return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token, newUser.getEmail()));
        }
        return ResponseEntity.badRequest().body(new ErrorResponseDTO("Error while registering. Please verify if the email is already in use or contact the support team."));
    }
}
