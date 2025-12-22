package imbuy.user.domain.service;

import imbuy.user.domain.model.User;

public class UserPolicy {

    public void requireSelfOrSupervisor(Long targetUserId, User requester) {
        if (requester == null) {
            throw new IllegalStateException("Unauthorized");
        }
        if (!requester.isSupervisor() && !requester.getId().equals(targetUserId)) {
            throw new IllegalStateException("Forbidden");
        }
    }
}
