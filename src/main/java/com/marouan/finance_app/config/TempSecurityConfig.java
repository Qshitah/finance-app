package com.marouan.finance_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// TEMPORARY: wide open so I can test with curl, only on the local profile
// gets deleted when the real login/JWT part is built, never let this reach docker or prod
@Configuration
@Profile("local")
public class TempSecurityConfig {

    @Bean
    SecurityFilterChain open(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}