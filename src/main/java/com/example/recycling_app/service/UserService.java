package com.example.recycling_app.service;

import com.example.recycling_app.domain.User;
import com.example.recycling_app.dto.UserSignupRequest;
import com.example.recycling_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserSignupRequest req) {
        try {
            if (userRepository.findByEmail(req.getEmail()).isPresent()) {
                throw new IllegalStateException("이미 존재하는 이메일입니다.");
            }

            String uid = "local-" + req.getEmail();
            String hashed = passwordEncoder.encode(req.getPassword());

            User user = User.builder()
                    .uid(uid)
                    .provider("local")
                    .email(req.getEmail())
                    .password(hashed)
                    .name(req.getName())
                    .phoneNumber(req.getPhoneNumber())
                    .age(req.getAge())
                    .gender(req.getGender())
                    .region(req.getRegion())
                    .build();

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("회원가입 실패: " + e.getMessage());
        }
    }

    // 이메일 찾기 (이름 + 전화번호 기반)
    public String findEmail(String name, String phoneNumber) {
        try {
            return userRepository.findByNameAndPhoneNumber(name, phoneNumber)
                    .map(User::getEmail)
                    .orElseThrow(() -> new IllegalStateException("일치하는 사용자를 찾을 수 없습니다."));
        } catch (Exception e) {
            throw new RuntimeException("이메일 찾기 중 오류 발생: " + e.getMessage());
        }
    }
}

