package com.example.my_api_server.controller;

public record MemberSignupDto(
        String email,
        String password
) {
}
