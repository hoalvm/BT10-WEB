package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.dto.auth.LoginRequest;
import vn.iotstar.dto.auth.LoginResponse;
import vn.iotstar.dto.auth.RegisterRequest;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.JwtService;

import java.util.Map;

/**
 * REST controller cho JWT authentication (Bai 4).
 * Endpoints:
 *   POST /auth/signup — dang ky tai khoan
 *   POST /auth/login  — dang nhap, tra JWT token
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Dang ky tai khoan moi.
     * Tuong tu slide Trang 26: POST /auth/signup
     */
    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> signup(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email da ton tai"));
        }

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER chua duoc tao"));

        // Tao username tu email (phan truoc @)
        String baseUsername = request.getEmail().split("@")[0];
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsernameIgnoreCase(username)) {
            username = baseUsername + suffix++;
        }

        User newUser = User.builder()
                .username(username)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(defaultRole)
                .enabled(true)
                .build();

        User saved = userRepository.save(newUser);

        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "username", saved.getUsername(),
                "email", saved.getEmail(),
                "fullName", saved.getFullName(),
                "role", saved.getRole().getName(),
                "enabled", saved.isEnabled()
        ));
    }

    /**
     * Dang nhap va tra JWT token.
     * Tuong tu slide Trang 26: POST /auth/login
     */
    @PostMapping("/login")
    @Transactional(readOnly = true)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);

        LoginResponse response = new LoginResponse(jwtToken, jwtService.getExpirationTime());
        return ResponseEntity.ok(response);
    }
}
