//package com.example.recycling_app.service;
//
//import com.example.recycling_app.domain.User;
//import com.example.recycling_app.dto.GoogleSignupRequest;
//import com.example.recycling_app.dto.JwtLoginResponse;
//import com.example.recycling_app.repository.UserRepository;
//import com.example.recycling_app.util.JwtUtil;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseAuthException;
//import com.google.firebase.auth.FirebaseToken;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//@Service
//@RequiredArgsConstructor
//public class GoogleAuthService {
//
//    private final UserRepository userRepository;
//    private final JwtUtil jwtUtil;
//    private final PasswordEncoder passwordEncoder;
//
//    public void signupWithGoogle(GoogleSignupRequest req) {
//        try {
//            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(req.getIdToken());
//            String uid = token.getUid();
//            String email = token.getEmail();
//
//            if (userRepository.findByEmail(email).isPresent()) {
//                throw new IllegalStateException("이미 가입된 이메일입니다.");
//            }
//
//            User user = User.builder()
//                    .uid(uid)
//                    .provider("google")
//                    .email(email)
//                    .password(passwordEncoder.encode(req.getPassword()))
//                    .name(req.getName())
//                    .phoneNumber(req.getPhoneNumber())
//                    .age(req.getAge())
//                    .gender(req.getGender())
//                    .region(req.getRegion())
//                    .build();
//
//            userRepository.save(user);
//        } catch (FirebaseAuthException e) {
//            throw new RuntimeException("Google 인증 실패: " + e.getMessage());
//        } catch (Exception e) {
//            throw new RuntimeException("회원가입 처리 실패: " + e.getMessage());
//        }
//    }
//
//    public JwtLoginResponse loginWithGoogle(String idToken) {
//        try {
//            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
//            String uid = token.getUid();
//            String email = token.getEmail();
//
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "등록된 사용자가 아닙니다. 먼저 회원가입하세요."));
//
//            if (!"google".equalsIgnoreCase(user.getProvider())) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이 이메일은 일반 가입 계정입니다. 이메일 로그인으로 시도하십시오.");
//            }
//
//            String accessToken = jwtUtil.createAccessToken(uid, email);
//            String refreshToken = jwtUtil.createRefreshToken(uid, email);
//
//            return JwtLoginResponse.builder()
//                    .accessToken(accessToken)
//                    .refreshToken(refreshToken)
//                    .tokenType("Bearer")
//                    .expiresIn(3600)
//                    .build();
//
//        } catch (FirebaseAuthException e) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Google ID Token입니다.");
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 처리 실패: " + e.getMessage());
//        }
//    }
//}
//
