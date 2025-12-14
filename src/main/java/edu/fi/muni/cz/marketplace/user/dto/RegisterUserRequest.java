package edu.fi.muni.cz.marketplace.user.dto;

public record RegisterUserRequest(
    String userId,
    String firstName,
    String lastName,
    String email,
    String phoneNumber) {
}
