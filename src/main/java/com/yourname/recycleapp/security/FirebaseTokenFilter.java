package com.yourname.recycleapp.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// Firebase ID 토큰을 검증하는 필터
@Slf4j // Lombok을 사용하여 로그 기능 추가
public class FirebaseTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");

        // 토큰이 없거나 "Bearer "로 시작하지 않으면 다음 필터로 넘김
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = authorizationHeader.substring(7); // "Bearer " 제외한 실제 토큰

        try {
            // 2. Firebase Admin SDK를 사용하여 ID 토큰 검증
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            // 3. 검증된 토큰에서 사용자 정보(UID) 추출
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail(); // 이메일도 필요시 추출

            // 4. Spring Security 컨텍스트에 사용자 정보 저장
            // 여기서는 간단하게 UserDetails 객체를 생성하여 저장합니다.
            // 실제 앱에서는 데이터베이스에서 사용자 정보를 조회하여 더 상세한 UserDetails를 구성할 수 있습니다.
            UserDetails userDetails = User.builder()
                    .username(uid) // UID를 username으로 사용
                    .password("") // 비밀번호는 토큰 기반 인증이므로 필요 없음
                    .authorities(Collections.singletonList(() -> "ROLE_USER")) // 기본 역할 부여
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 현재 요청의 SecurityContext에 Authentication 객체 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5. 요청 애트리뷰트에 UID 저장 (컨트롤러/서비스에서 쉽게 접근 가능)
            request.setAttribute("uid", uid);
            log.info("Firebase ID Token 검증 성공. UID: {}", uid);

        } catch (FirebaseAuthException e) {
            log.error("Firebase ID Token 검증 실패: {}", e.getMessage());
            // 토큰이 유효하지 않으면 401 Unauthorized 응답
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid Firebase ID token.");
            return; // 필터 체인 중단
        } catch (Exception e) {
            log.error("Firebase ID Token 처리 중 예상치 못한 오류 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An unexpected error occurred during token verification.");
            return; // 필터 체인 중단
        }

        // 다음 필터 또는 서블릿으로 요청 전달
        filterChain.doFilter(request, response);
    }
}