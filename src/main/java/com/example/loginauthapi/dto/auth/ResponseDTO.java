package com.example.loginauthapi.dto.auth;

public record ResponseDTO (String name, String token, String email, String city, String professionalRegister, int professionalType, String state) { }
