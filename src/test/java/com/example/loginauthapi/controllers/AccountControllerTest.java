package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.dto.user.UserInfoRequestDTO;
import com.example.loginauthapi.dto.user.UserInfoResponseDTO;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.services.JsonCitiesService;
import com.example.loginauthapi.services.JsonStatesService;
import com.example.loginauthapi.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private JsonStatesService jsonStatesService;

    @Mock
    private JsonCitiesService jsonCitiesService;

    private AccountController accountController;

    private User user;
    private String validToken;
    private String authorizationHeader;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Manually instantiate AccountController with mocked dependencies
        accountController = new AccountController(userService, tokenService, jsonStatesService, jsonCitiesService);

        user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setCity("São Paulo");
        user.setState("SP");
        user.setProfessionalType(0);
        user.setProfessionalRegister("12345");

        validToken = "validToken";
        authorizationHeader = "Bearer " + validToken;
    }

// Testes para getAccountInfo

    @Test
    public void testGetAccountInfo_Success() {
        when(tokenService.validateToken(anyString())).thenReturn(user.getEmail());
        when(userService.getByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<UserInfoResponseDTO> response = accountController.getAccountInfo(authorizationHeader);

        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(user.getEmail(), response.getBody().email());
        assertEquals(user.getName(), response.getBody().name());
        assertEquals(user.getCity(), response.getBody().city());
        assertEquals(user.getState(), response.getBody().state());
        assertEquals(user.getProfessionalType(), response.getBody().professionalType());
        assertEquals(user.getProfessionalRegister(), response.getBody().professionalRegister());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(userService, times(1)).getByEmail(user.getEmail());
    }

    @Test
    public void testGetAccountInfo_InvalidToken() {
        when(tokenService.validateToken(validToken)).thenReturn(null);

        ResponseEntity<UserInfoResponseDTO> response = accountController.getAccountInfo(authorizationHeader);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(userService, never()).getByEmail(anyString());
    }

    @Test
    public void testGetAccountInfo_UserNotFound() {
        when(tokenService.validateToken(validToken)).thenReturn(user.getEmail());
        when(userService.getByEmail(user.getEmail())).thenReturn(Optional.empty());

        ResponseEntity<UserInfoResponseDTO> response = accountController.getAccountInfo(authorizationHeader);

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(userService, times(1)).getByEmail(user.getEmail());
    }

    @Test
    public void testGetAccountInfo_Exception() {
        when(tokenService.validateToken(validToken)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<UserInfoResponseDTO> response = accountController.getAccountInfo(authorizationHeader);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(userService, never()).getByEmail(anyString());
    }

    // Testes para updateAccountInfo

    @Test
    public void testUpdateAccountInfo_Success() throws IOException {
        UserInfoRequestDTO requestDTO = new UserInfoRequestDTO(
                "example@user.com", "John Doe", "Rio de Janeiro", "RJ", 0, "123456789"
        );

        when(tokenService.validateToken(validToken)).thenReturn(user.getEmail());
        when(jsonStatesService.isStateSiglaValid("RJ")).thenReturn(true);
        when(jsonStatesService.getStateIdBySigla("RJ")).thenReturn("33"); // Exemplo de ID para RJ
        when(jsonCitiesService.isCityValid("Rio de Janeiro", "33")).thenReturn(true);
        when(userService.getByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<UserInfoResponseDTO> response = accountController.updateAccountInfo(authorizationHeader, requestDTO);

        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(user.getEmail(), response.getBody().email());
        assertEquals("John Doe", response.getBody().name());
        assertEquals("Rio de Janeiro", response.getBody().city());
        assertEquals("RJ", response.getBody().state());
        assertEquals(0, response.getBody().professionalType());
        assertEquals("123456789", response.getBody().professionalRegister());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(jsonStatesService, times(1)).isStateSiglaValid("RJ");
        verify(jsonStatesService, times(1)).getStateIdBySigla("RJ");
        verify(jsonCitiesService, times(1)).isCityValid("Rio de Janeiro", "33");
        verify(userService, times(1)).getByEmail(user.getEmail());
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateAccountInfo_InvalidToken() throws IOException {
        UserInfoRequestDTO requestDTO = new UserInfoRequestDTO(
                "example@user.com", "John Doe", "Rio de Janeiro", "RJ", 0, "123456789"
        );

        when(tokenService.validateToken(validToken)).thenReturn(null);

        ResponseEntity<UserInfoResponseDTO> response = accountController.updateAccountInfo(authorizationHeader, requestDTO);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(jsonStatesService, never()).isStateSiglaValid(anyString());
        verify(userService, never()).getByEmail(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test @Disabled
    public void testUpdateAccountInfo_InvalidState() throws IOException {
        UserInfoRequestDTO requestDTO = new UserInfoRequestDTO(
                "example@user.com", "John Doe", "Rio de Janeiro", "RJ", 0, "123456789"
        );

        when(tokenService.validateToken(validToken)).thenReturn(user.getEmail());
        when(jsonStatesService.isStateSiglaValid("InvalidState")).thenReturn(false);

        ResponseEntity<UserInfoResponseDTO> response = accountController.updateAccountInfo(authorizationHeader, requestDTO);

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(jsonStatesService, times(1)).isStateSiglaValid("InvalidState");
        verify(jsonCitiesService, never()).isCityValid(anyString(), anyString());
        verify(userService, never()).getByEmail(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    public void testUpdateAccountInfo_InvalidCity() throws IOException {
        UserInfoRequestDTO requestDTO = new UserInfoRequestDTO(
                "example@user.com", "John Doe", "Rio de Janeiro", "RJ", 0, "123456789"
        );

        when(tokenService.validateToken(validToken)).thenReturn(user.getEmail());
        when(jsonStatesService.isStateSiglaValid("RJ")).thenReturn(true);
        when(jsonStatesService.getStateIdBySigla("RJ")).thenReturn("33");
        when(jsonCitiesService.isCityValid("InvalidCity", "33")).thenReturn(false);

        ResponseEntity<UserInfoResponseDTO> response = accountController.updateAccountInfo(authorizationHeader, requestDTO);

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(jsonStatesService, times(1)).isStateSiglaValid("RJ");
        verify(jsonStatesService, times(1)).getStateIdBySigla("RJ");
        verify(jsonCitiesService, times(1)).isCityValid("InvalidCity", "33");
        verify(userService, never()).getByEmail(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    public void testUpdateAccountInfo_UserNotFound() throws IOException {
        UserInfoRequestDTO requestDTO = new UserInfoRequestDTO(
                "example@user.com", "John Doe", "Rio de Janeiro", "RJ", 0, "123456789"
        );

        when(tokenService.validateToken(validToken)).thenReturn(user.getEmail());
        when(jsonStatesService.isStateSiglaValid("RJ")).thenReturn(true);
        when(jsonStatesService.getStateIdBySigla("RJ")).thenReturn("33");
        when(jsonCitiesService.isCityValid("Rio de Janeiro", "33")).thenReturn(true);
        when(userService.getByEmail(user.getEmail())).thenReturn(Optional.empty());

        ResponseEntity<UserInfoResponseDTO> response = accountController.updateAccountInfo(authorizationHeader, requestDTO);

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(jsonStatesService, times(1)).isStateSiglaValid("RJ");
        verify(jsonStatesService, times(1)).getStateIdBySigla("RJ");
        verify(jsonCitiesService, times(1)).isCityValid("Rio de Janeiro", "33");
        verify(userService, times(1)).getByEmail(user.getEmail());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    public void testUpdateAccountInfo_Exception() throws IOException {
        UserInfoRequestDTO requestDTO = new UserInfoRequestDTO(
                "example@user.com", "John Doe", "Rio de Janeiro", "RJ", 0, "123456789"
        );

        when(tokenService.validateToken(validToken)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<UserInfoResponseDTO> response = accountController.updateAccountInfo(authorizationHeader, requestDTO);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(tokenService, times(1)).validateToken(validToken);
        verify(jsonStatesService, never()).isStateSiglaValid(anyString());
        verify(userService, never()).getByEmail(anyString());
        verify(userService, never()).save(any(User.class));
    }
}
