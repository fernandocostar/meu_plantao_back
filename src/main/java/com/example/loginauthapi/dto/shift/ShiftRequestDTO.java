package com.example.loginauthapi.dto.shift;

import java.time.LocalDateTime;


public record ShiftRequestDTO (LocalDateTime startTime, LocalDateTime endTime, double value, long location) {
}
