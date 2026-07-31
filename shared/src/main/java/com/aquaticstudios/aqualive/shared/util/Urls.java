package com.aquaticstudios.aqualive.shared.util;

import java.util.Locale;

public final class Urls {
    public static String normalize(final String input) {
        final String trimmed = input.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed;
        return "https://" + trimmed;
    }

    public static String domain(final String url) {
        String clean = url;

        final int scheme = clean.indexOf("://");
        if (scheme >= 0) clean = clean.substring(scheme + 3);

        final int at = clean.indexOf('@');
        if (at >= 0) clean = clean.substring(at + 1);

        final int slash = clean.indexOf('/');
        if (slash >= 0) clean = clean.substring(0, slash);

        final int colon = clean.indexOf(':');
        if (colon >= 0) clean = clean.substring(0, colon);

        return clean.toLowerCase(Locale.ROOT);
    }
}
