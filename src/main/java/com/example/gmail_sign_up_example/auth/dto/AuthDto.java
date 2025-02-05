package com.example.gmail_sign_up_example.auth.dto;

import lombok.Builder;

public final class AuthDto {
    @Builder
    public record SignUpRequest(
            String username,
            String password,
            String email,
            String nickname,
            String otpVerificationToken
    ) {}

    @Builder
    public record VerifyOtpRequest(
            String email,
            String otp,
            String otpRequestToken
    ) {}
}