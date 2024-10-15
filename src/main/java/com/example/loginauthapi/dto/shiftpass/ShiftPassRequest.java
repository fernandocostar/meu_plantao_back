package com.example.loginauthapi.dto.shiftpass;

import java.util.List;

public record ShiftPassRequest(Long shiftId, List<String> offeredUsers) {
}
