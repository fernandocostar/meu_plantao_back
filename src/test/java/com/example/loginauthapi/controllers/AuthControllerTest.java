package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.dto.ErrorResponseDTO;
import com.example.loginauthapi.dto.auth.AuthLoginRequestDTO;
import com.example.loginauthapi.dto.auth.AuthRegisterRequestDTO;
import com.example.loginauthapi.dto.auth.ResponseDTO;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        AuthLoginRequestDTO requestDTO = new AuthLoginRequestDTO("user@example.com", "password");
        User user = new User();
        user.setName("John Doe");
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

        when(userService.getByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateToken(any(User.class))).thenReturn("token");

        ResponseEntity<?> response = authController.login(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token", ((ResponseDTO) response.getBody()).token());
        assertEquals("John Doe", ((ResponseDTO) response.getBody()).name());
        assertEquals("user@example.com", ((ResponseDTO) response.getBody()).email());
        verify(userService, times(1)).getByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(tokenService, times(1)).generateToken(any(User.class));
    }

    @Test
    void testLogin_InvalidCredentials() {
        AuthLoginRequestDTO requestDTO = new AuthLoginRequestDTO("user@example.com", "wrongPassword");
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

        when(userService.getByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ResponseEntity<?> response = authController.login(requestDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password, please try again", ((ErrorResponseDTO) response.getBody()).error());

        verify(userService, times(1)).getByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(tokenService, never()).generateToken(any(User.class));
    }

    @Test
    void testLogin_UserNotFound() {
        AuthLoginRequestDTO requestDTO = new AuthLoginRequestDTO("user@example.com", "password");

        when(userService.getByEmail(anyString())).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.login(requestDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password, please try again", ((ErrorResponseDTO) response.getBody()).error());

        verify(userService, times(1)).getByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenService, never()).generateToken(any(User.class));
    }

    @Test
    void testRegister_Success() {
        AuthRegisterRequestDTO requestDTO = new AuthRegisterRequestDTO("John Doe","user@example.com", "password", 0, "1234567", "RJ", "Rio de Janeiro");
        User user = new User();
        user.setEmail("user@example.com");
        user.setName("John Doe");

        when(userService.getByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.save(any(User.class))).thenReturn(user);
        when(tokenService.generateToken(any(User.class))).thenReturn("token");

        ResponseEntity<?> response = authController.register(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token", ((ResponseDTO) response.getBody()).token());
        assertEquals("John Doe", ((ResponseDTO) response.getBody()).name());
        assertEquals("user@example.com", ((ResponseDTO) response.getBody()).email());

        verify(userService, times(1)).getByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userService, times(1)).save(any(User.class));
        verify(tokenService, times(1)).generateToken(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyInUse() {
        AuthRegisterRequestDTO requestDTO = new AuthRegisterRequestDTO("John Doe","user@example.com", "password", 0, "1234567", "RJ", "Rio de Janeiro");
        User user = new User();
        user.setEmail("user@example.com");

        when(userService.getByEmail(anyString())).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authController.register(requestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, times(1)).getByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).save(any(User.class));
        verify(tokenService, never()).generateToken(any(User.class));
    }

}