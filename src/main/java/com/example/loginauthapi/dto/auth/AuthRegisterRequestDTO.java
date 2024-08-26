package com.example.loginauthapi.dto.auth;

public record AuthRegisterRequestDTO(String name, String email, String password, int professionalType, String professionalRegister, String state, String city) {
}
