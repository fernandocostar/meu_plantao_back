package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.dto.shift.ShiftRequestDTO;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.repositories.LocationRepository;
import com.example.loginauthapi.repositories.ShiftRepository;
import com.example.loginauthapi.repositories.UserRepository;
import com.example.loginauthapi.services.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    @Autowired
    ShiftService shiftService;
    @Autowired
    TokenService tokenService;

    @Autowired
    LocationRepository locationRepository;
    @Autowired
    ShiftRepository shiftRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping(value = "/getAll", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Shift>> getAll(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try {
            if (!this.hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());

            String token = extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);

            if(userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }

            List<Shift> userShifts = shiftService.findByUser(userRepository.findByEmail(userEmail).get());

            return ResponseEntity.ok(userShifts);

        } catch (Exception e) {
            log.error("Error getting shifts", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }
    }

    @PostMapping(value = "/createShift", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Shift> createShift(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody ShiftRequestDTO body) {
        try {
            if (!this.hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

                String token = extractAuthorizationToken(authorizationHeader);
                String userEmail = tokenService.validateToken(token);

                if(userEmail == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                }

                Shift newShift = new Shift();
                newShift.setStartTime(body.startTime());
                newShift.setEndTime(body.endTime());
                newShift.setValue(body.value());
                newShift.setLocation(locationRepository.findById(body.location()).get());
                newShift.setUser(userRepository.findByEmail(userEmail).get());
                this.shiftRepository.save(newShift);

                return ResponseEntity.ok(newShift);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping(value = "/deleteShift/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> deleteShift(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable Long id) {
        try {
            if (!hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

            String token = extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);

            if(userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Optional<Shift> optionalShift = shiftRepository.findById(id);
            if (optionalShift.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            Shift shift = optionalShift.get();

            if(Boolean.TRUE.equals(shift.getPassing())) {
                log.info(String.format("[%s] Shift is passing, cannot delete", shift.getId()));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            if(shift.getUser().getEmail().equals(userEmail)) {
                shiftRepository.delete(shift);
                return ResponseEntity.ok(id + " shift deleted");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping(value = "/updateShift/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Shift> updateShift(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable Long id, @RequestBody ShiftRequestDTO body) {
        try {
            if (!hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

            String token = this.extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);

            if(userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Optional<Shift> optionalShift = shiftRepository.findById(id);
            if (optionalShift.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            Shift shift = optionalShift.get();

            if(Boolean.TRUE.equals(shift.getPassing())) {
                log.info("Shift %s is passing, cannot update", shift.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            if(shift.getUser().getEmail().equals(userEmail)) {
                shift.setStartTime(body.startTime());
                shift.setEndTime(body.endTime());
                shift.setValue(body.value());
                shift.setLocation(locationRepository.findById(body.location()).get());
                this.shiftRepository.save(shift);
                return ResponseEntity.ok(shift);
            } else {
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
