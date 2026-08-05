package com.twistedmomos.backend.auth.service;

import com.twistedmomos.backend.auth.dto.response.UserAdminResponse;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface UserAdminService {

    PageResponse<UserAdminResponse> list(Pageable pageable);

    UserAdminResponse grant(Long actorId, Long subjectId, String roleName);

    UserAdminResponse revoke(Long actorId, Long subjectId, String roleName);
}
