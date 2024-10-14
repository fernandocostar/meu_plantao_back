package com.example.loginauthapi.dto;

import com.example.loginauthapi.domain.user.User;

import java.util.List;

public record ShiftPassRequest(Long shiftId, List<String> offeredUsers) {
}
