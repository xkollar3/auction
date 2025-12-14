package edu.fi.muni.cz.marketplace.user.dto;

public record RegisterUserRequest(
    String nickname,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String password) {
}
