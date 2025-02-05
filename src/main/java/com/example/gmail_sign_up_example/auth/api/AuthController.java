package com.example.gmail_sign_up_example.auth.api;

import com.example.gmail_sign_up_example.auth.dto.AuthDto.SignUpRequest;
import com.example.gmail_sign_up_example.auth.dto.AuthDto.VerifyOtpRequest;
import com.example.gmail_sign_up_example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestParam String email) throws Exception {
        var otpRequestToken = authService.sendOtp(email);
        return ResponseEntity.ok(otpRequestToken);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) {
        var otpVerificationToken = authService.verifyOtp(verifyOtpRequest);
        return ResponseEntity.ok(otpVerificationToken);
    }

    @PostMapping("/signUp")
    public ResponseEntity<Void> signUp(@RequestBody SignUpRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.noContent().build();
    }
}
