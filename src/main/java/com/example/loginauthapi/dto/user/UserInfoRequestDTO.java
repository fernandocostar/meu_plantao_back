package com.example.loginauthapi.dto.user;

public record UserInfoRequestDTO(String email, String name, String city, String state, int professionalType, String professionalRegister) {
}
