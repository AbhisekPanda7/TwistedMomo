package com.twistedmomos.backend.auth.controller;

import com.twistedmomos.backend.auth.dto.request.GrantRoleRequest;
import com.twistedmomos.backend.auth.dto.response.UserAdminResponse;
import com.twistedmomos.backend.auth.security.CustomUserDetails;
import com.twistedmomos.backend.auth.service.UserAdminService;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Every endpoint here requires ROLE_ADMIN — enforced in SecurityConfig via the /api/v1/admin/** matcher. */
@Tag(name = "Admin — Users", description = "User listing and role assignment (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping
    public ResponseEntity<PageResponse<UserAdminResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userAdminService.list(pageable));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<UserAdminResponse> grant(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody GrantRoleRequest request) {
        return ResponseEntity.ok(userAdminService.grant(principal.getUser().getId(), id, request.role()));
    }

    @DeleteMapping("/{id}/roles/{role}")
    public ResponseEntity<UserAdminResponse> revoke(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @PathVariable String role) {
        return ResponseEntity.ok(userAdminService.revoke(principal.getUser().getId(), id, role));
    }
}
