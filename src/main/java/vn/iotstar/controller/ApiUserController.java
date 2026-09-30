package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.UserService;

import java.util.List;
import java.util.Map;

/**
 * REST controller tra ve thong tin user qua JWT Bearer token (Bai 4).
 * Endpoints:
 *   GET /api/users/me  — thong tin user dang dang nhap
 *   GET /api/users/    — danh sach tat ca users (admin)
 *
 * Tuong tu slide Trang 27.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ApiUserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Tra ve thong tin user hien tai tu JWT.
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of(
                "id", principal.getId(),
                "username", principal.getUsername(),
                "email", principal.getEmail(),
                "fullName", principal.getFullName() != null ? principal.getFullName() : "",
                "images", principal.getImages() != null ? principal.getImages() : "",
                "role", principal.getRoleName(),
                "enabled", principal.isEnabled()
        ));
    }

    /**
     * Tra ve danh sach tat ca users.
     * GET /api/users/
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> allUsers() {
        List<UserDTO> users = userService.findAll(null, 0, Integer.MAX_VALUE).getContent();
        return ResponseEntity.ok(users);
    }
}
