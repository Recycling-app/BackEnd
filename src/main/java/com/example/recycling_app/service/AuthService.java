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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // 이메일 발송 서비스

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

    public void resetPassword(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("가입된 계정을 찾을 수 없습니다."));

            // 1. 임시 비밀번호 생성
            String tempPassword = UUID.randomUUID().toString().substring(0, 10); // 예: 10자리

            // 2. 비밀번호 암호화
            String encoded = passwordEncoder.encode(tempPassword);
            user.setPassword(encoded);

            // 3. Firebase Firestore에 비밀번호 재설정
            userRepository.save(user);

            // 4. 이메일 발송
            String subject = "[재활용 앱] 임시 비밀번호 안내";
            String content = """
                안녕하세요, 분리수거 서비스입니다.

                요청하신 임시 비밀번호는 다음과 같습니다:

                ▶ %s

                로그인 후 꼭 마이페이지에서 비밀번호를 변경해주세요.

                감사합니다.
                """.formatted(tempPassword);

            emailService.send(email, subject, content);

        } catch (Exception e) {
            throw new RuntimeException("비밀번호 재설정 실패: " + e.getMessage());
        }
    }
}
