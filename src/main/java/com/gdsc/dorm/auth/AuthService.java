package com.gdsc.dorm.auth;

import com.gdsc.dorm.auth.data.dto.req.LoginReq;
import com.gdsc.dorm.auth.data.dto.req.SignUpReq;
import com.gdsc.dorm.auth.data.dto.res.LoginRes;
import com.gdsc.dorm.config.jwt.TokenProvider;
import com.gdsc.dorm.jwt.RefreshTokenRepository;
import com.gdsc.dorm.jwt.data.RefreshToken;
import com.gdsc.dorm.member.MemberRepository;
import com.gdsc.dorm.member.data.Member;
import com.gdsc.dorm.member.data.dto.res.MemberGetRes;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder pwEncoder;
    private final TokenProvider tokenProvider;

    public ResponseEntity<MemberGetRes> signUp(SignUpReq req) {
        if(memberRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("signup: 이미 존재하는 사용자입니다.");
        }

        String bcryptPw = pwEncoder.encode(req.getPassword());
        Member newMember = Member.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(bcryptPw)
                .gender(req.getGender())
                .dorm(req.getDorm())
                .birthYear(req.getBirthYear())
                .studentId(req.getStudentId())
                .build();

        memberRepository.save(newMember);

        return new ResponseEntity<>(new MemberGetRes(newMember), HttpStatus.CREATED);
    }

    public void logout(HttpServletRequest request) {
        String token = tokenProvider.getAccessToken(request.getHeader("Authorization"));

        Long memberId = tokenProvider.getUserId(token);
        if(refreshTokenRepository.existsByMemberId(memberId)) {
            refreshTokenRepository.deleteByMemberId(memberId);
        }

        SecurityContextHolder.clearContext();
    }
}
