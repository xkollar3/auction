package edu.fi.muni.cz.marketplace.user.dto;

public record TokenResponse(String accessToken, String tokenType, Integer expiresIn, String refreshToken) {
}
