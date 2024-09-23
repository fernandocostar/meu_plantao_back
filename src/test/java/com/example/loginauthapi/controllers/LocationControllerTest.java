package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.location.Location;
import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.dto.location.LocationRequestDTO;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.services.LocationService;
import com.example.loginauthapi.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationControllerTest {

    @InjectMocks
    private LocationController locationController;

    @Mock
    private LocationService locationService;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    private String validToken;
    private String authorizationHeader;
    private User user;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        validToken = "valid-token";
        authorizationHeader = "Bearer " + validToken;
        user = new User();
        user.setEmail("user@example.com");

    }

    @Test
    public void testGetUserLocations_Success() {
        Location location = new Location();
        location.setId(1L);
        location.setName("Test Location");
        location.setUser(user);

        when(locationService.findByUser(any(User.class))).thenReturn(Collections.singletonList(location));
        when(tokenService.validateToken(validToken)).thenReturn(user.getEmail());
        when(userService.getByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<List<Location>> response = locationController.getUserLocations(authorizationHeader);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Location", response.getBody().get(0).getName());
    }

    @Test
    public void testGetUserLocations_Unauthorized() {
        // Simulate invalid token
        String invalidAuthorizationHeader = "Bearer invalid-token";

        when(tokenService.validateToken(anyString())).thenReturn(null);

        ResponseEntity<List<Location>> response = locationController.getUserLocations(invalidAuthorizationHeader);

        assertEquals(401, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    public void testCreateLocation_Success() {
        LocationRequestDTO requestDTO = new LocationRequestDTO("New Location");
        Location location = new Location();
        location.setId(1L);
        location.setName(requestDTO.name());
        location.setUser(user);
        location.setActive(true);

        when(locationService.save(any(Location.class))).thenReturn(location);
        when(tokenService.validateToken(validToken)).thenReturn(user.getEmail());
        when(userService.getByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<Location> response = locationController.createLocation(authorizationHeader, requestDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("New Location", response.getBody().getName());
    }

    @Test
    public void testDeleteLocation_Success() {
        Long locationId = 1L;
        Location location = new Location();
        location.setId(locationId);
        location.setUser(user);
        location.setActive(true);

        when(locationService.findById(locationId)).thenReturn(Optional.of(location));
        when(locationService.save(any(Location.class))).thenReturn(location);
        when(tokenService.validateToken(validToken)).thenReturn(user.getEmail());

        ResponseEntity<String> response = locationController.deleteLocation(authorizationHeader, locationId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(locationId + "location deleted", response.getBody());
    }

    @Test
    public void testUpdateLocation_Success() {
        Long locationId = 1L;
        LocationRequestDTO requestDTO = new LocationRequestDTO("Updated Location");
        Location location = new Location();
        location.setId(locationId);
        location.setUser(user);
        location.setName("Old Name");

        when(locationService.findById(locationId)).thenReturn(Optional.of(location));
        when(locationService.save(any(Location.class))).thenReturn(location);
        when(tokenService.validateToken(validToken)).thenReturn(user.getEmail());

        ResponseEntity<Location> response = locationController.updateLocation(authorizationHeader, locationId, requestDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Updated Location", response.getBody().getName());
    }

}
