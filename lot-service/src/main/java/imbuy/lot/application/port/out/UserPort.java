package imbuy.lot.application.port.out;

import imbuy.lot.application.dto.UserDto;

public interface UserPort {
    UserDto getUserById(Long id);
}
