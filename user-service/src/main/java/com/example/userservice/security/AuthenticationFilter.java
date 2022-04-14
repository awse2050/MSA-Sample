package com.example.userservice.security;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UsersService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@Log4j2
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UsersService usersService;
    private Environment env;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UsersService usersService,
                                Environment env
                                ){
        super.setAuthenticationManager(authenticationManager);
        this.usersService = usersService;
        this.env = env;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        try {
            RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            // UsernamePasswordAuthenticationToken으로 변환
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(),
                            creds.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 실제 로그인 성공 후 어떤 처리를 해줄것인가?
    // 토큰 만료시간이 얼마나있는지, 반환값은 뭘 줄것인지 등등
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        // 사용자 이름을 추출.
        String username = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDetails = usersService.getUserDetailsByEmail(username);
        // JWT 토큰 추가설정
        String token = Jwts.builder()
                .setSubject(userDetails.getUserId()) //userId로 토큰을 만든다.
                // 하루짜리 토큰을 만든다.
                .setExpiration(
                        new Date(System.currentTimeMillis() +
                                Long.parseLong(env.getProperty("token.expiration_time"))))
                // 암호화를 위한 알고리즘을 추가, 조합키로 시크릿값을 추가
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
                .compact();

        // 헤더에 집어넣어야 한다.
        response.addHeader("token", token);
        // 정상적으로 만들어진건지 확인
        response.addHeader("userId", userDetails.getUserId());

    }
}
