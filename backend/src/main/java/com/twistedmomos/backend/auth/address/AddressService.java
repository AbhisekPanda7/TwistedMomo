package com.twistedmomos.backend.auth.address;

import com.twistedmomos.backend.auth.dto.response.AddressResponse;
import com.twistedmomos.backend.auth.entity.UserAddress;
import com.twistedmomos.backend.auth.repository.UserAddressRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import com.twistedmomos.backend.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Remembers where a customer has delivered, so checkout can offer it back. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    /** Bounded so the list stays useful without a management screen to prune it. */
    public static final int MAX_SAVED = 5;

    /** Joins normalized fields; not "|" because freeform address text can contain it. */
    private static final String FIELD_DELIMITER = "\u0001";

    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public void remember(
            Long userId, String recipientName, String phone,
            String line1, String line2, String city, String postalCode) {

        String key = normalize(line1, line2, city, postalCode);
        Instant now = Instant.now();

        var existing = addressRepository.findByUserIdAndNormalizedKey(userId, key);
        if (existing.isPresent()) {
            UserAddress address = existing.get();
            // Details can drift — a corrected phone or recipient on the same street.
            address.setRecipientName(recipientName);
            address.setPhone(phone);
            address.setLastUsedAt(now);
            log.debug("Saved address reused: userId={} addressId={}", userId, address.getId());
            return;
        }

        addressRepository.save(UserAddress.builder()
                .user(userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found")))
                .recipientName(recipientName)
                .phone(phone)
                .addressLine1(line1)
                .addressLine2(line2)
                .city(city)
                .postalCode(postalCode)
                .normalizedKey(key)
                .lastUsedAt(now)
                .build());

        // Fetch one past the cap; anything returned is surplus.
        List<UserAddress> surplus =
                addressRepository.findByUserIdOrderByLastUsedAtDesc(userId, PageRequest.of(MAX_SAVED, 1));
        if (!surplus.isEmpty()) {
            addressRepository.deleteAll(surplus);
            log.debug("Pruned saved addresses beyond the cap: userId={} removed={}", userId, surplus.size());
        }
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listRecent(Long userId) {
        return addressRepository.findByUserIdOrderByLastUsedAtDesc(userId, PageRequest.of(0, MAX_SAVED))
                .stream()
                .map(a -> new AddressResponse(
                        a.getId(), a.getRecipientName(), a.getPhone(),
                        a.getAddressLine1(), a.getAddressLine2(),
                        a.getCity(), a.getPostalCode(), a.getLastUsedAt()))
                .toList();
    }

    /** Scoped by userId, so another customer's address is indistinguishable from a missing one. */
    @Transactional(readOnly = true)
    public UserAddress findOwned(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> {
                    log.warn("Address not found or not owned: addressId={} userId={}", addressId, userId);
                    return new ResourceNotFoundException("Address " + addressId + " not found");
                });
    }

    /**
     * Case and whitespace only. Expanding "St" to "Street" would silently merge two
     * genuinely different addresses, which is worse than showing one twice.
     */
    private static String normalize(String line1, String line2, String city, String postalCode) {
        return String.join(FIELD_DELIMITER,
                squash(line1), squash(line2), squash(city), squash(postalCode));
    }

    private static String squash(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
