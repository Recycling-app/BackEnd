package com.example.recycling_app.service;

import com.example.recycling_app.domain.User;
import com.example.recycling_app.dto.UserSignupRequest;
import com.example.recycling_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void signup(String uid, String provider, UserSignupRequest dto) {
        try {
            if (userRepository.existsByUid(uid)) {
                throw new IllegalStateException("이미 등록된 사용자입니다.");
            }

            User user = User.builder()
                    .uid(uid)
                    .provider(provider)
                    .email(dto.getEmail())
                    .name(dto.getName())
                    .age(dto.getAge())
                    .gender(dto.getGender())
                    .region(dto.getRegion())
                    .build();

            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("회원가입 중 오류가 발생했습니다.", e);
        }
    }
}
