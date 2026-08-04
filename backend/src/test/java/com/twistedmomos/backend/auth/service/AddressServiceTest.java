package com.twistedmomos.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.auth.entity.UserAddress;
import com.twistedmomos.backend.auth.repository.UserAddressRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import com.twistedmomos.backend.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    /** Mirrors AddressService's private field delimiter, so key assertions stay exact. */
    private static final String DELIM = "\u0001";

    @Mock private UserAddressRepository addressRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AddressService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Tester").email("t@example.com").build();
    }

    /** Case and spacing differ, the address does not — one row, not two. */
    @Test
    void treatsCaseAndSpacingDifferencesAsTheSameAddress() {
        UserAddress existing = addressOf(9L, "1 test st" + DELIM + "cuttack" + DELIM + "753014");
        when(addressRepository.findByUserIdAndNormalizedKey(eq(1L), anyString()))
                .thenReturn(Optional.of(existing));

        service.remember(1L, "Tester", "9999999999", "1 Test  St ", null, "Cuttack", "753014");

        verify(addressRepository, never()).save(argThat(a -> a.getId() == null));
        assertThat(existing.getLastUsedAt()).isNotNull();
    }

    /** Abbreviations are left alone: guessing would merge genuinely different addresses. */
    @Test
    void treatsStreetAndStAsDifferentAddresses() {
        when(addressRepository.findByUserIdAndNormalizedKey(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByUserIdOrderByLastUsedAtDesc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of());

        service.remember(1L, "Tester", "9999999999", "1 Test Street", null, "Cuttack", "753014");

        ArgumentCaptor<UserAddress> saved = ArgumentCaptor.forClass(UserAddress.class);
        verify(addressRepository).save(saved.capture());
        assertThat(saved.getValue().getNormalizedKey())
                .isEqualTo("1 test street" + DELIM + DELIM + "cuttack" + DELIM + "753014");
    }

    /** A "|" inside a field must not let two different addresses collide onto one key. */
    @Test
    void treatsFieldsContainingThePipeCharacterAsDistinctAddresses() {
        when(addressRepository.findByUserIdAndNormalizedKey(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByUserIdOrderByLastUsedAtDesc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of());

        service.remember(1L, "Tester", "9999999999", "A|B", "C", "Cuttack", "753014");
        service.remember(1L, "Tester", "9999999999", "A", "B|C", "Cuttack", "753014");

        ArgumentCaptor<UserAddress> saved = ArgumentCaptor.forClass(UserAddress.class);
        verify(addressRepository, times(2)).save(saved.capture());
        List<String> keys = saved.getAllValues().stream().map(UserAddress::getNormalizedKey).toList();
        assertThat(keys.get(0)).isNotEqualTo(keys.get(1));
    }

    @Test
    void prunesBeyondTheCap() {
        when(addressRepository.findByUserIdAndNormalizedKey(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserAddress oldest = addressOf(2L, "old");
        // Match only the exact "one row past the cap" page — a regressed offset must fail this test.
        when(addressRepository.findByUserIdOrderByLastUsedAtDesc(
                        anyLong(), eq(PageRequest.of(AddressService.MAX_SAVED, 1))))
                .thenReturn(List.of(oldest));

        service.remember(1L, "Tester", "9999999999", "9 New Rd", null, "Cuttack", "753014");

        verify(addressRepository).deleteAll(List.of(oldest));
    }

    @Test
    void listsTheCallersAddressesMostRecentlyUsedFirst() {
        when(addressRepository.findByUserIdOrderByLastUsedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(addressOf(3L, "a"), addressOf(4L, "b")));

        assertThat(service.listRecent(1L)).extracting(r -> r.id()).containsExactly(3L, 4L);
    }

    /** 404 rather than 403: whether someone else's address exists must not leak. */
    @Test
    void reportsAnotherUsersAddressAsNotFound() {
        when(addressRepository.findByIdAndUserId(77L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findOwned(1L, 77L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private UserAddress addressOf(Long id, String key) {
        return UserAddress.builder()
                .id(id).user(user).recipientName("Tester").phone("9999999999")
                .addressLine1("1 Test St").city("Cuttack").postalCode("753014")
                .normalizedKey(key).lastUsedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
    }
}
