package com.marouan.finance_app.web;

import com.marouan.finance_app.repository.UserRepository;
import com.marouan.finance_app.web.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // every method here needs an admin JWT, checked before the method runs
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserResponse> list(@RequestParam(required = false) Boolean enabled) {
        var users = enabled == null ? userRepository.findAll() : userRepository.findByEnabled(enabled);
        return users.stream().map(UserResponse::from).toList();
    }

    @PatchMapping("/{id}/enable")
    public UserResponse enable(@PathVariable UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        user.setEnabled(true);
        return UserResponse.from(userRepository.save(user));
    }
}