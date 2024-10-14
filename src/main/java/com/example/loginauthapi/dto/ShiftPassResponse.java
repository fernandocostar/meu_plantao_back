package com.example.loginauthapi.dto;

public record ShiftPassResponse (String userEmail, Long originalShiftId, Long shiftPassId, String message) {
}
