package com.mhrs.auth.application.port.out;

import com.mhrs.auth.domain.AuthenticatedUser;

public interface CurrentUserProvider {

    AuthenticatedUser currentUser();

    String currentSessionId();
}
