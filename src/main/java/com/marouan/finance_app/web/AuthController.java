package com.marouan.finance_app.web;

import com.marouan.finance_app.security.AppUserDetails;
import com.marouan.finance_app.security.JwtService;
import com.marouan.finance_app.web.dto.LoginRequest;
import com.marouan.finance_app.web.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        // this checks the password AND the enabled flag, throws if either fails
        // the exception handler below turns it into a clean 401/403
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        var principal = (AppUserDetails) auth.getPrincipal();
        var role = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String token = jwtService.generate(principal.getUsername(), principal.getId(), role);
        return LoginResponse.of(token);
    }
}