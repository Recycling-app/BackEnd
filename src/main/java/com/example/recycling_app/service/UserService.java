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

            String encryptedPassword = passwordEncoder.encode(req.getPassword());

            // uid는 이메일 자체를 사용하거나 UUID 생성도 가능
            String uid = "local-" + req.getEmail();

            User user = User.builder()
                    .uid(uid)
                    .provider("local")
                    .email(req.getEmail())
                    .name(req.getName())
                    .password(encryptedPassword)
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
}

