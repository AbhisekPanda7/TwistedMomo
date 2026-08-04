package com.twistedmomos.backend.auth.controller;

import com.twistedmomos.backend.auth.dto.response.AddressResponse;
import com.twistedmomos.backend.auth.security.CustomUserDetails;
import com.twistedmomos.backend.auth.service.AddressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Addresses", description = "Delivery addresses this customer has used before")
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /** No address-by-id route: the caller's own list is the only thing worth exposing. */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> listMine(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(addressService.listRecent(principal.getUser().getId()));
    }
}
