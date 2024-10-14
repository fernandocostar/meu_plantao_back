package com.example.loginauthapi.controllers;

import com.example.loginauthapi.domain.location.Location;
import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.domain.shiftpass.ShiftPass;
import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.dto.ShiftPassRequest;
import com.example.loginauthapi.dto.ShiftPassResponse;
import com.example.loginauthapi.infra.security.TokenService;
import com.example.loginauthapi.services.LocationService;
import com.example.loginauthapi.services.ShiftPassService;
import com.example.loginauthapi.services.ShiftService;
import com.example.loginauthapi.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/shifts/pass")
@RequiredArgsConstructor
public class ShiftPassController {

    @Autowired
    TokenService tokenService;

    @Autowired
    ShiftPassService shiftPassService;

    @Autowired
    ShiftService shiftService;

    @Autowired
    UserService userService;

    @Autowired
    LocationService locationService;

    @GetMapping(value = "/get/{shift_pass_id}")
    public ResponseEntity<ShiftPass> getShiftPass(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable("shift_pass_id") Long shiftPassId) {

        log.info("Getting shift pass");

        if (!hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        String token = extractAuthorizationToken(authorizationHeader);
        String userEmail = tokenService.validateToken(token);

        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Optional<ShiftPass> optionalShiftPass = shiftPassService.findById(shiftPassId);

        if (optionalShiftPass.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(optionalShiftPass.get());
    }

    @PostMapping(value = "/create/")
    public ResponseEntity<ShiftPassResponse> createShiftPass(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody ShiftPassRequest requestBody) {

        if (!hasAuthorization(authorizationHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        String token = extractAuthorizationToken(authorizationHeader);
        String userEmail = tokenService.validateToken(token);

        log.info("Creating shift pass for user email %s", userEmail);

        if(userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Optional<Shift> optionalShift = shiftService.findById(requestBody.shiftId());
        if(optionalShift.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Shift originShift = optionalShift.get();

        if(Boolean.TRUE.equals(originShift.getPassing())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ShiftPassResponse(userEmail, originShift.getId(), null, "Shift already has a pass"));
        }

        List<User> offeredUsers = userService.getUsersListByIds(requestBody.offeredUsers());

        ShiftPass shiftPass = shiftPassService.createShiftPass(originShift.getId(), userEmail, offeredUsers);

        if(shiftPass == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        return ResponseEntity.ok(new ShiftPassResponse(userEmail, shiftPass.getOriginalShiftId(), shiftPass.getId(), "Shift Pass created successfully"));

    }

    @DeleteMapping(value = "/delete/{shift_pass_id}")
    public ResponseEntity<ShiftPassResponse> deleteShiftPass(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable("shift_pass_id") Long shiftPassId) {

        try {
            log.info("Deleting originShift pass");
            if (!hasAuthorization(authorizationHeader))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

            String token = extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);

            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Optional<ShiftPass> optionalShiftPass = shiftPassService.findById(shiftPassId);
            if (optionalShiftPass.isEmpty()) {
                log.info("Shift pass not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Optional<Shift> optionalOriginShift = shiftService.findById(optionalShiftPass.get().getOriginalShiftId());
            if (optionalOriginShift.isEmpty()) {
                log.info("Origin shift not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Shift originShift = optionalOriginShift.get();
            ShiftPass shiftPass = optionalShiftPass.get();

            if (!shiftPass.getCreatedBy().getEmail().equals(userEmail)) {
                log.info("User is not the creator of the shift pass");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            if (!shiftPass.isActive()) {
                log.info("Shift pass is not active");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            shiftPassService.deleteShiftPassAndUpdateShift(shiftPass, originShift);

            return ResponseEntity.ok(new ShiftPassResponse(userEmail, shiftPass.getOriginalShiftId(), shiftPass.getId(), "Shift Pass deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(value = "/accept/{shiftPassId}")
    public ResponseEntity<ShiftPassResponse> acceptShiftPass(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable("shiftPassId") Long shiftPassId,
            @RequestParam("locationId") Long locationId) {

        log.info("Accepting shift pass");

        try {
            if (!hasAuthorization(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            String token = extractAuthorizationToken(authorizationHeader);
            String userEmail = tokenService.validateToken(token);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Optional<ShiftPass> optionalShiftPass = shiftPassService.findById(shiftPassId);
            if (optionalShiftPass.isEmpty()) {
                log.info("Shift pass not found %s", shiftPassId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            log.info("Shift pass found " + shiftPassId.toString());

            Optional<User> optionalUser = userService.getByEmail(userEmail);
            if (optionalUser.isEmpty()) {
                log.info("User not found " + userEmail);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Optional<Location> optionalLocation = locationService.findById(Long.valueOf(locationId));
            if (optionalLocation.isEmpty()) {
                log.info("Location not found " + locationId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Location location = optionalLocation.get();
            ShiftPass shiftPass = optionalShiftPass.get();
            User user = optionalUser.get();

            Optional<Shift> optionalOriginShift = shiftService.findById(shiftPass.getOriginalShiftId());
            if (optionalOriginShift.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Shift originShift = optionalOriginShift.get();

            if (shiftPass.getOfferedUsers().stream().anyMatch(u -> u.getId().equals(user.getId()))) {

                Shift newShift = new Shift();
                newShift.setStartTime(originShift.getStartTime());
                newShift.setEndTime(originShift.getEndTime());
                newShift.setValue(originShift.getValue());
                newShift.setLocation(location);
                newShift.setUser(user);

                shiftPass.setFinalUser(user);
                shiftPass.setActive(false);

                shiftPassService.updateOriginalShiftAndShiftPassAndCreateNewShift(shiftPass, newShift, originShift);

                return ResponseEntity.ok(new ShiftPassResponse(userEmail, shiftPass.getOriginalShiftId(), shiftPass.getId(), "Shift Pass accepted successfully"));
            }

        } catch (NumberFormatException e) {
            log.error("Invalid location ID format", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Erro de formato de ID
        } catch (Exception e) {
            log.error("Error accepting shift pass", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }


    public boolean hasAuthorization(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }

    public String extractAuthorizationToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

}
