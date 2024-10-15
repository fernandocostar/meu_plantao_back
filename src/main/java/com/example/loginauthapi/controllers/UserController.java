package com.example.loginauthapi.controllers;

import com.example.loginauthapi.infra.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final TokenService tokenService;

    @GetMapping(value = "get", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> getUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

                String token = authorizationHeader.substring(7);
                String userEmail = tokenService.validateToken(token);

                if(userEmail == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
                }

                return ResponseEntity.ok(userEmail);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}

