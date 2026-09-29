package com.marouan.finance_app.web;

import com.marouan.finance_app.domain.User;
import com.marouan.finance_app.repository.UserRepository;
import com.marouan.finance_app.security.AppUserDetails;
import com.marouan.finance_app.security.JwtService;
import com.marouan.finance_app.service.UserService;
import com.marouan.finance_app.web.dto.LoginRequest;
import com.marouan.finance_app.web.dto.LoginResponse;
import com.marouan.finance_app.web.dto.SignupRequest;
import com.marouan.finance_app.web.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));

            var principal = (AppUserDetails) auth.getPrincipal();
            var role = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            String token = jwtService.generate(principal.getUsername(), principal.getId(), role);
            return LoginResponse.of(token);

        } catch (org.springframework.security.authentication.DisabledException e) {
            // wrong password would say the same thing, so an attacker can't tell disabled vs wrong password
            throw new org.springframework.security.access.AccessDeniedException("Account is pending admin approval");
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid username or password");
        }
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest req) {
        var user = userService.signup(req.username(), req.email(), req.password());
        return UserResponse.from(user);
    }
}