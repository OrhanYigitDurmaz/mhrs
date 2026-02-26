package com.mhrs.patient.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public final class VerificationRules {

    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    private VerificationRules() {}

    public static void validate(String identityNumber, String documentUrl) {
        if (identityNumber == null || identityNumber.isBlank()) {
            throw new IllegalArgumentException("identityNumber is required");
        }
        int length = identityNumber.length();
        if (length < 6 || length > 20) {
            throw new IllegalArgumentException(
                "identityNumber length must be between 6 and 20"
            );
        }
        if (!ID_PATTERN.matcher(identityNumber).matches()) {
            throw new IllegalArgumentException(
                "identityNumber must be alphanumeric"
            );
        }
        if (documentUrl == null || documentUrl.isBlank()) {
            throw new IllegalArgumentException("documentUrl is required");
        }
        try {
            URI uri = new URI(documentUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("documentUrl must be valid");
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("documentUrl must be valid", ex);
        }
    }
}
