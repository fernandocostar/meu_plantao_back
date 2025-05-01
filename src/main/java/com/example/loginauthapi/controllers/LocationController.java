package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.location.Location;
import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.dto.location.LocationRequestDTO;
import com.example.loginauthapi.dto.shift.ShiftRequestDTO;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.repositories.LocationRepository;
import com.example.loginauthapi.repositories.ShiftRepository;
import com.example.loginauthapi.repositories.UserRepository;
import com.example.loginauthapi.services.LocationService;
import com.example.loginauthapi.services.ShiftService;
import com.example.loginauthapi.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

    private static final Logger log = LoggerFactory.getLogger(LocationController.class);
    @Autowired
    LocationService locationService;

    @Autowired
    TokenService tokenService;

    @Autowired
    UserService userService;

    @GetMapping(value = "/getUserLocations", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Location>> getUserLocations(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try {
            if (!this.hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());

            String token = extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);

            if(userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }

            List<Location> userLocations = locationService.findByUser(userService.getByEmail(userEmail).get());

            return ResponseEntity.ok(userLocations);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }
    }

    @PostMapping(value = "/createLocation", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Location> createLocation(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody LocationRequestDTO body) {
        try {
            if (!this.hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

                String token = extractAuthorizationToken(authorizationHeader);
                String userEmail = tokenService.validateToken(token);

                if(userEmail == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                }

                Location newLocation = new Location();
                newLocation.setName(body.name());
                newLocation.setUser(userService.getByEmail(userEmail).get());
                newLocation.setActive(true);
                this.locationService.save(newLocation);

                return ResponseEntity.ok(newLocation);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping(value = "/deleteLocation/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> deleteLocation(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable Long id) {
        try {
            if (!hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

            String token = extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);

            if(userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Location location = locationService.findById(id).get();

            if(location.getUser().getEmail().equals(userEmail)) {
                location.setActive(false);
                locationService.save(location);
                return ResponseEntity.ok(id + "location deleted");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping(value = "/updateLocation/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Location> updateLocation(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable Long id, @RequestBody LocationRequestDTO body) {
        try {
            if (!hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

            String token = this.extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);
            System.out.println(userEmail);
            if(userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Optional<Location> optionalLocation = locationService.findById(id);
            if (optionalLocation.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            Location location = optionalLocation.get();

            if(location.getUser().getEmail().equals(userEmail)) {
                location.setName(body.name());
                this.locationService.save(location);
                return ResponseEntity.ok(location);
            } else {
                System.out.println(location.getUser().getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    public boolean hasAuthorization(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }

    public String extractAuthorizationToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

}
