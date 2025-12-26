package imbuy.user.domain.service;

import imbuy.user.infrastructure.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserPolicy {

    public void requireSelfOrSupervisor(Long targetUserId, UserPrincipal requester) {
        if (requester == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (!requester.isSupervisor() && !requester.getId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }
}
