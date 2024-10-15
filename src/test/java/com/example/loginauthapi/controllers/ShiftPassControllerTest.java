package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.location.Location;
import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.domain.shiftpass.ShiftPass;
import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.dto.shiftpass.ShiftPassRequest;
import com.example.loginauthapi.dto.shiftpass.ShiftPassActionResponse;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.services.LocationService;
import com.example.loginauthapi.services.ShiftPassService;
import com.example.loginauthapi.services.ShiftService;
import com.example.loginauthapi.services.UserService;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShiftPassControllerTest {

    @InjectMocks
    private ShiftPassController shiftPassController;

    @Mock
    private TokenService tokenService;

    @Mock
    private ShiftPassService shiftPassService;

    @Mock
    private ShiftService shiftService;

    @Mock
    private UserService userService;

    @Mock
    private LocationService locationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetShiftPassSuccess() {
        // Arrange
        Long shiftPassId = 1L;
        String authorizationHeader = "Bearer valid_token";
        String userEmail = "user@example.com";

        ShiftPass shiftPass = new ShiftPass();
        when(tokenService.validateToken("valid_token")).thenReturn(userEmail);
        when(shiftPassService.findById(shiftPassId)).thenReturn(Optional.of(shiftPass));

        // Act
        ResponseEntity<ShiftPass> response = shiftPassController.getShiftPass(authorizationHeader, shiftPassId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(shiftPass, response.getBody());
    }

    @Test
    public void testGetShiftPassUnauthorized() {
        // Arrange
        String authorizationHeader = "Bearer invalid_token";

        when(tokenService.validateToken(anyString())).thenReturn(null);

        // Act
        ResponseEntity<ShiftPass> response = shiftPassController.getShiftPass(authorizationHeader, 1L);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testCreateShiftPassSuccess() {
        // Arrange
        String authorizationHeader = "Bearer valid_token";
        String userEmail = "user@example.com";
        ShiftPassRequest request = new ShiftPassRequest(1L, List.of("1234", "4321"));
        Shift shift = new Shift();
        shift.setPassing(false);

        ShiftPass shiftPass = new ShiftPass();
        shiftPass.setOriginalShiftId(1L);
        shiftPass.setId(1L);

        when(tokenService.validateToken("valid_token")).thenReturn(userEmail);
        when(shiftService.findById(1L)).thenReturn(Optional.of(shift));
        when(userService.getUsersListByIds(request.offeredUsers())).thenReturn(List.of(createRandomUser(), createRandomUser()));
        when(shiftPassService.createShiftPass(anyLong(), anyString(), anyList())).thenReturn(shiftPass);

        // Act
        ResponseEntity<ShiftPassActionResponse> response = shiftPassController.createShiftPass(authorizationHeader, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Shift Pass created successfully", response.getBody().message());
    }

    @Test
    public void testCreateShiftPassShiftNotFound() {
        // Arrange
        String authorizationHeader = "Bearer valid_token";
        ShiftPassRequest request = new ShiftPassRequest(1L, List.of("1234", "4321"));

        when(tokenService.validateToken(anyString())).thenReturn("user@example.com");
        when(shiftService.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ShiftPassActionResponse> response = shiftPassController.createShiftPass(authorizationHeader, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteShiftPassSuccess() {
        // Arrange
        User creator = createRandomUser();
        String authorizationHeader = "Bearer valid_token";
        String userEmail = creator.getEmail();
        ShiftPass shiftPass = new ShiftPass();
        Shift shift = new Shift();
        shiftPass.setCreatedBy(creator);
        shiftPass.setActive(true);

        when(tokenService.validateToken("valid_token")).thenReturn(userEmail);
        when(shiftPassService.findById(1L)).thenReturn(Optional.of(shiftPass));
        when(shiftService.findById(shiftPass.getOriginalShiftId())).thenReturn(Optional.of(shift));

        // Act
        ResponseEntity<ShiftPassActionResponse> response = shiftPassController.deleteShiftPass(authorizationHeader, 1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Shift Pass deleted successfully", response.getBody().message());
    }

    @Test
    public void testAcceptShiftPassSuccess() {

        User creator = createRandomUser();
        User acceptor = createRandomUser();
        // Arrange
        String authorizationHeader = "Bearer valid_token";
        Long shiftPassId = 1L;
        Long locationId = 1L;
        String userEmail = acceptor.getEmail();

        ShiftPass shiftPass = new ShiftPass();
        shiftPass.setCreatedBy(creator);
        shiftPass.setOriginalShiftId(1L);
        shiftPass.setOfferedUsers(List.of(acceptor, createRandomUser()));

        Shift originShift = new Shift();
        Location location = new Location();
        User user = new User();
        user.setId("1234");

        when(tokenService.validateToken("valid_token")).thenReturn(userEmail);
        when(shiftPassService.findById(shiftPassId)).thenReturn(Optional.of(shiftPass));
        when(userService.getByEmail(userEmail)).thenReturn(Optional.of(acceptor));
        when(locationService.findById(locationId)).thenReturn(Optional.of(location));
        when(shiftService.findById(shiftPass.getOriginalShiftId())).thenReturn(Optional.of(originShift));

        // Act
        ResponseEntity<ShiftPassActionResponse> response = shiftPassController.acceptShiftPass(authorizationHeader, shiftPassId, locationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Shift Pass accepted successfully", response.getBody().message());
    }

    private User createRandomUser() {
        Faker faker = new Faker();
        User user = new User();
        user.setId(faker.idNumber().valid());
        user.setEmail(faker.internet().emailAddress());
        user.setName(faker.name().fullName());
        user.setPassword(faker.internet().password());
        return user;
    }

}
