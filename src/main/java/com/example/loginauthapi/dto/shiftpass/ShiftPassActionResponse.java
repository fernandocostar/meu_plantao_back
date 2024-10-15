package com.example.loginauthapi.dto.shiftpass;

public record ShiftPassActionResponse(String userEmail, Long originalShiftId, Long shiftPassId, String message) {
}
