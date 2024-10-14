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

    // Helper to validate authorization and return user email
    private Optional<String> validateAuthorization(String authorizationHeader) {
        if (!hasAuthorization(authorizationHeader)) {
            return Optional.empty();
        }
        String token = extractAuthorizationToken(authorizationHeader);
        String userEmail = tokenService.validateToken(token);
        return Optional.ofNullable(userEmail);
    }

    // Helper to log actions related to ShiftPass
    private void logShiftPassAction(String action, Long shiftPassId) {
        log.info("[{}] {}", shiftPassId, action);
    }

    private void logUserAction(String action, String userEmail) {
        log.info("[{}] {}", userEmail, action);
    }

    // Helper to get a shift and return appropriate response if not found
    private ResponseEntity<Shift> getShiftOrNotFound(Long shiftId) {
        Optional<Shift> optionalShift = shiftService.findById(shiftId);
        if (optionalShift.isEmpty()) {
            log.info("[{}] Shift not found", shiftId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.of(optionalShift);
    }

    @GetMapping(value="/get/offeredShifts")
    public ResponseEntity<List<ShiftPass>> getOfferedShifts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        Optional<String> userEmailOpt = validateAuthorization(authorizationHeader);
        if (userEmailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userEmail = userEmailOpt.get();

        logUserAction("Getting offered shifts", userEmail);

        List<ShiftPass> offeredShifts = shiftPassService.getOfferedShiftsByUserEmail(userEmail);

        logUserAction("Offered shifts returned", userEmail);
        return ResponseEntity.ok(offeredShifts);
    }

    @GetMapping(value = "/get/{shift_pass_id}")
    public ResponseEntity<ShiftPass> getShiftPass(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable("shift_pass_id") Long shiftPassId) {

        logShiftPassAction("Getting shift pass", shiftPassId);

        Optional<String> userEmailOpt = validateAuthorization(authorizationHeader);
        if (userEmailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<ShiftPass> optionalShiftPass = shiftPassService.findById(shiftPassId);
        if (optionalShiftPass.isEmpty()) {
            logShiftPassAction("Shift pass not found", shiftPassId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        ShiftPass shiftPass = optionalShiftPass.get();
        logShiftPassAction("Shift pass returned", shiftPassId);
        log.debug(shiftPass.toString());

        return ResponseEntity.ok(shiftPass);
    }

    @PostMapping(value = "/create/")
    public ResponseEntity<ShiftPassResponse> createShiftPass(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody ShiftPassRequest requestBody) {

        Optional<String> userEmailOpt = validateAuthorization(authorizationHeader);
        if (userEmailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userEmail = userEmailOpt.get();

        log.info("[{}] Creating shift pass for user", userEmail);

        ResponseEntity<Shift> shiftResponse = getShiftOrNotFound(requestBody.shiftId());
        if (!shiftResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Shift originShift = shiftResponse.getBody();

        if (Boolean.TRUE.equals(originShift.getPassing())) {
            log.info("[{}] Shift already has a pass", originShift.getId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ShiftPassResponse(userEmail, originShift.getId(), null, "Shift already has a pass"));
        }

        List<User> offeredUsers = userService.getUsersListByIds(requestBody.offeredUsers());
        ShiftPass shiftPass = shiftPassService.createShiftPass(originShift.getId(), userEmail, offeredUsers);

        if (shiftPass == null) {
            log.info("Error creating shift pass");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        log.info("[{}] Shift pass created successfully", shiftPass.getId());
        return ResponseEntity.ok(new ShiftPassResponse(userEmail, shiftPass.getOriginalShiftId(), shiftPass.getId(), "Shift Pass created successfully"));
    }

    @DeleteMapping(value = "/delete/{shift_pass_id}")
    public ResponseEntity<ShiftPassResponse> deleteShiftPass(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable("shift_pass_id") Long shiftPassId) {

        try {
            log.info("Deleting originShift pass");

            Optional<String> userEmailOpt = validateAuthorization(authorizationHeader);
            if (userEmailOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String userEmail = userEmailOpt.get();

            Optional<ShiftPass> optionalShiftPass = shiftPassService.findById(shiftPassId);
            if (optionalShiftPass.isEmpty()) {
                log.info("Shift pass not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            ShiftPass shiftPass = optionalShiftPass.get();

            Optional<Shift> optionalOriginShift = shiftService.findById(shiftPass.getOriginalShiftId());
            if (optionalOriginShift.isEmpty()) {
                log.info("Origin shift not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Shift originShift = optionalOriginShift.get();

            if (!shiftPass.getCreatedBy().getEmail().equals(userEmail)) {
                log.info("User is not the creator of the shift pass");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (!shiftPass.isActive()) {
                log.info("Shift pass is not active");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            shiftPassService.deleteShiftPassAndUpdateShift(shiftPass, originShift);
            return ResponseEntity.ok(new ShiftPassResponse(userEmail, shiftPass.getOriginalShiftId(), shiftPass.getId(), "Shift Pass deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/accept/{shiftPassId}")
    public ResponseEntity<ShiftPassResponse> acceptShiftPass(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable("shiftPassId") Long shiftPassId,
            @RequestParam("locationId") Long locationId) {

        logShiftPassAction("Accepting shift pass", shiftPassId);

        try {
            Optional<String> userEmailOpt = validateAuthorization(authorizationHeader);
            if (userEmailOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String userEmail = userEmailOpt.get();

            Optional<User> optionalUser = userService.getByEmail(userEmail);
            if (optionalUser.isEmpty()) {
                log.info("[{}] User not found", userEmail);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            User user = optionalUser.get();

            Optional<ShiftPass> optionalShiftPass = shiftPassService.findById(shiftPassId);
            if (optionalShiftPass.isEmpty()) {
                log.info("[{}] Shift pass not found", shiftPassId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            ShiftPass shiftPass = optionalShiftPass.get();

            Optional<Location> optionalLocation = locationService.findById(locationId);
            if (optionalLocation.isEmpty()) {
                log.info("[{}] Location not found", locationId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Location location = optionalLocation.get();

            Optional<Shift> optionalOriginShift = shiftService.findById(shiftPass.getOriginalShiftId());
            if (optionalOriginShift.isEmpty()) {
                log.info("[{}] Origin shift not found", shiftPass.getOriginalShiftId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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

                log.info("[{}] Shift pass accepted successfully", shiftPassId);
                return ResponseEntity.ok(new ShiftPassResponse(userEmail, shiftPass.getOriginalShiftId(), shiftPass.getId(), "Shift Pass accepted successfully"));
            } else {
                log.info("[{}] User not found in offered users", shiftPassId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

        } catch (NumberFormatException e) {
            log.error("Invalid location ID format", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Invalid ID format error
        } catch (Exception e) {
            log.error("Error accepting shift pass", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public boolean hasAuthorization(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }

    public String extractAuthorizationToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

}
