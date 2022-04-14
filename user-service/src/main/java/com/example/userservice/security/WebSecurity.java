package com.example.userservice.security;

import com.example.userservice.service.UsersService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private UsersService usersService;
    private BCryptPasswordEncoder passwordEncoder;
    private Environment env;

    public WebSecurity(UsersService usersService,
                       BCryptPasswordEncoder passwordEncoder
    , Environment env) {
        this.usersService = usersService;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    // 권한 관련 작업
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
//            .authorizeRequests().antMatchers("/users/**").permitAll()
            .authorizeRequests().antMatchers("/**")
                .hasIpAddress("192.168.10.210")
                .and()
                // 필터를 추가시킨다.
                .addFilter(getAuthenticationFilter());

        http.headers().frameOptions().disable();
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {

        AuthenticationFilter authenticationFilter
                = new AuthenticationFilter(authenticationManager(), usersService, env);

        return authenticationFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(usersService).passwordEncoder(passwordEncoder);
    }
}
