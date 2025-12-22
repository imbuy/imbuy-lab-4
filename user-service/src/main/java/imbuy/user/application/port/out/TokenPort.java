package imbuy.user.application.port.out;

import imbuy.user.domain.model.User;

public interface TokenPort {

    String generateAccess(User user);
    String generateRefresh(User user);

    boolean isValid(String token, String username);
    boolean isRefresh(String token);
    String extractUsername(String token);
}
