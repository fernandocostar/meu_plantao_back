package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.dto.shift.ShiftRequestDTO;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.repositories.ShiftRepository;
import com.example.loginauthapi.repositories.UserRepository;
import com.example.loginauthapi.services.ShiftService;
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
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private static final Logger log = LoggerFactory.getLogger(ShiftController.class);
    @Autowired
    ShiftService shiftService;
    @Autowired
    TokenService tokenService;

    @Autowired
    ShiftRepository shiftRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/getAll")
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }
    }

    @PostMapping("/createShift")
    public ResponseEntity<Shift> createShift(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody ShiftRequestDTO body) {
        try {
            if (!this.hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

                String token = extractAuthorizationToken(authorizationHeader);
                String userEmail = tokenService.validateToken(token);

                if(userEmail == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                }

                //front end should handle in case user already have a shift starting in the requested time

                Shift newShift = new Shift();
                newShift.setStartTime(body.startTime());
                newShift.setEndTime(body.endTime());
                newShift.setValue(body.value());
                newShift.setLocation(body.location());
                newShift.setUser(userRepository.findByEmail(userEmail).get());
                this.shiftRepository.save(newShift);

                return ResponseEntity.ok(newShift);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/deleteShift/{id}")
    public ResponseEntity<String> deleteShift(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable Long id) {
        try {
            if (!hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

            String token = extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);

            if(userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Shift shift = shiftRepository.findById(id).get();

            if(shift.getUser().getEmail().equals(userEmail)) {
                shiftRepository.delete(shift);
                return ResponseEntity.ok(id + "shift deleted");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/updateShift/{id}")
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

            if(shift.getUser().getEmail().equals(userEmail)) {
                shift.setStartTime(body.startTime());
                shift.setEndTime(body.endTime());
                shift.setValue(body.value());
                shift.setLocation(body.location());
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
