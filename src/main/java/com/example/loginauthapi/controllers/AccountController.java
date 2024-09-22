package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.dto.user.UserInfoRequestDTO;
import com.example.loginauthapi.dto.user.UserInfoResponseDTO;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.services.JsonCitiesService;
import com.example.loginauthapi.services.JsonStatesService;
import com.example.loginauthapi.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final UserService userService;
    private final TokenService tokenService;
    private final JsonStatesService jsonStatesService;
    private final JsonCitiesService jsonCitiesService;

    @GetMapping(value = "/info", produces = "application/json; charset=UTF-8")
    public ResponseEntity<UserInfoResponseDTO> getAccountInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try {
            String token = extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);

            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = userService.getByEmail(userEmail).orElseThrow(() -> {
                log.error("User not found: {}", userEmail);
                return new NoSuchElementException("User not found");
            });

            UserInfoResponseDTO responseDTO = new UserInfoResponseDTO(
                    user.getEmail(), user.getName(), user.getCity(), user.getState(),
                    user.getProfessionalType(), user.getProfessionalRegister()
            );

            return ResponseEntity.ok(responseDTO);

        } catch (NoSuchElementException e) {
            log.error("Error fetching user info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/update", produces = "application/json; charset=UTF-8")
    public ResponseEntity<UserInfoResponseDTO> updateAccountInfo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody UserInfoRequestDTO body) {

        try {
            String token = extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);

            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (!jsonStatesService.isStateSiglaValid(body.state())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            String stateId = jsonStatesService.getStateIdBySigla(body.state());

            if (!jsonCitiesService.isCityValid(body.city(), stateId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            User user = userService.getByEmail(userEmail).orElseThrow(() -> {
                log.error("User not found: {}", userEmail);
                return new NoSuchElementException("User not found");
            });

            user.setName(body.name());
            user.setCity(body.city());
            user.setState(body.state());
            user.setProfessionalType(body.professionalType());
            user.setProfessionalRegister(body.professionalRegister());

            userService.save(user); // Assuming UserService handles save operation

            UserInfoResponseDTO responseDTO = new UserInfoResponseDTO(
                    user.getEmail(), user.getName(), user.getCity(), user.getState(),
                    user.getProfessionalType(), user.getProfessionalRegister()
            );

            return ResponseEntity.ok(responseDTO);

        } catch (NoSuchElementException e) {
            log.error("Error updating user info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String extractAuthorizationToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
}
