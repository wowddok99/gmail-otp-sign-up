package com.example.gmail_sign_up_example.auth.service;

import com.example.gmail_sign_up_example.auth.dto.AuthDto.SignUpRequest;
import com.example.gmail_sign_up_example.auth.dto.AuthDto.VerifyOtpRequest;
import com.example.gmail_sign_up_example.auth.entity.User;
import com.example.gmail_sign_up_example.auth.entity.VerifiedEmail;
import com.example.gmail_sign_up_example.auth.entity.role.Role;
import com.example.gmail_sign_up_example.auth.repository.UserRepository;
import com.example.gmail_sign_up_example.auth.repository.VerifiedEmailRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final VerifiedEmailRepository verifiedEmailRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public String sendOtp(String email) throws Exception {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        
        // OTP 생성
        String otp = emailService.generateOtp();

        // 특정 OTP 요청에 대한 식별용 토큰(Secure Random) 생성
        String otpRequestToken = createSecureOtpToken();

        // 만료 시간 생성
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);
        
        VerifiedEmail verifiedEmail = VerifiedEmail.builder()
                .email(email)
                .otp(otp)
                .otpRequestToken(otpRequestToken)
                .otpExpiry(expiryTime)
                .build();

        verifiedEmailRepository.save(verifiedEmail);
        emailService.sendOtpEmail(email, otp); // 이메일 발송

        return otpRequestToken;
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest verifyOtpRequest) {
        VerifiedEmail fetchedVerifiedEmail = verifiedEmailRepository.findByEmail(verifyOtpRequest.email())
                .orElseThrow(() -> new NoSuchElementException("이메일이 존재하지 않습니다."));

        // OTP Request Token 검증
        if (!fetchedVerifiedEmail.getOtpRequestToken().equals(verifyOtpRequest.otpRequestToken())) {
            throw new RuntimeException("잘못된 요청입니다. 다시 시도해주세요.");
        }

        // OTP 만료 확인
        if (fetchedVerifiedEmail.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP가 만료되었습니다.");
        }

        // OTP 일치 여부 확인
        if (!fetchedVerifiedEmail.getOtp().equals(verifyOtpRequest.otp())) {
            throw new RuntimeException("잘못된 OTP입니다.");
        }

        // 인증 완료 후 토큰(Secure Token) 발급
        String otpVerificationToken = createSecureOtpToken();

        VerifiedEmail verifiedEmail = VerifiedEmail.builder()
                .id(fetchedVerifiedEmail.getId())
                .email(fetchedVerifiedEmail.getEmail())
                .otp(fetchedVerifiedEmail.getOtp())
                .otpRequestToken(verifyOtpRequest.otpRequestToken())
                .otpVerificationToken(otpVerificationToken) // 발급된 토큰 저장
                .otpExpiry(fetchedVerifiedEmail.getOtpExpiry())
                .build();

        verifiedEmailRepository.save(verifiedEmail);

        return otpVerificationToken;
    }

    @Transactional
    public void registerUser(SignUpRequest signUpRequest) {
        VerifiedEmail fetchedVerifiedEmail = verifiedEmailRepository.findByEmail(signUpRequest.email())
                .orElseThrow(() ->  new NoSuchElementException("이메일 인증이 필요합니다."));

            // 토큰 검증
            if (!fetchedVerifiedEmail.getOtpVerificationToken().equals(signUpRequest.otpVerificationToken())) {
                throw new RuntimeException("잘못된 인증 토큰입니다.");
            }

            User user = User.builder()
                    .username(signUpRequest.username())
                    .password(BCrypt.hashpw(signUpRequest.password(), BCrypt.gensalt()))
                    .email(signUpRequest.email())
                    .nickname(signUpRequest.nickname())
                    .role(Role.USER)
                    .isVerified(true)
                    .build();

            userRepository.save(user); // 유저 정보 저장
            verifiedEmailRepository.delete(fetchedVerifiedEmail); // 인증 정보 삭제
    }

    public String createSecureOtpToken() {
        // 랜덤 바이트 생성을 위해 SecureRandom 사용
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32]; // 256 bits (32 bytes)
        secureRandom.nextBytes(randomBytes);

        // 생성된 랜덤 바이트를 Base64로 인코딩하여 문자열로 변환
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
