package com.example.recycling_app.service;

import com.example.recycling_app.domain.User;
import com.example.recycling_app.dto.JwtLoginResponse;
import com.example.recycling_app.repository.UserRepository;
import com.example.recycling_app.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public JwtLoginResponse login(String email, String password) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 이메일입니다."));

            if (!"local".equalsIgnoreCase(user.getProvider())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "구글로 가입된 계정입니다. 구글 로그인을 이용해주세요.");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
            }

            String accessToken = jwtUtil.createAccessToken(user.getUid(), email);
            String refreshToken = jwtUtil.createRefreshToken(user.getUid(), email);

            return JwtLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600)
                    .build();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 처리 중 오류: " + e.getMessage());
        }
    }
}
