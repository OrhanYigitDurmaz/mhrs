package com.mhrs.auth.application.port.out;

import com.mhrs.auth.domain.UserAccount;
import java.util.Optional;

public interface UserAccountRepository {

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findById(String userId);

    UserAccount save(UserAccount userAccount);

    UserAccount update(UserAccount userAccount);
}
