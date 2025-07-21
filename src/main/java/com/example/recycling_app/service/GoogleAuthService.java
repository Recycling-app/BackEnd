package com.example.recycling_app.service;

import com.example.recycling_app.domain.User;
import com.example.recycling_app.dto.GoogleSignupRequest;
import com.example.recycling_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;

    public void signupWithGoogle(GoogleSignupRequest req) {
        try {
            if (userRepository.existsByUid(req.getUid())) {
                throw new IllegalStateException("이미 가입된 구글 계정입니다.");
            }

            User user = User.builder()
                    .uid(req.getUid())
                    .provider("google")
                    .email(req.getEmail())
                    .name(req.getName())
                    .password(null) // 소셜 회원은 비밀번호 없음
                    .phoneNumber(req.getPhoneNumber())
                    .age(req.getAge())
                    .gender(req.getGender())
                    .region(req.getRegion())
                    .build();

            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("구글 회원가입 실패: " + e.getMessage());
        }
    }
}
