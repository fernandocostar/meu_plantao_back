package com.example.loginauthapi.dto.user;

public record UserInfoResponseDTO (String email, String name, String city, String state, int professionalType, String professionalRegister) {
}
