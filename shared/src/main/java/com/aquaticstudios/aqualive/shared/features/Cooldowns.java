package com.aquaticstudios.aqualive.shared.features;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Cooldowns {
    private final Map<String, Long> expiry = new ConcurrentHashMap<>();

    public int remaining(final UUID uuid, final String platform) {
        final Long at = expiry.get(key(uuid, platform));
        if (at == null) return 0;
        final long left = at - System.currentTimeMillis();
        return left <= 0 ? 0 : (int) Math.ceil(left / 1000.0);
    }

    public void start(final UUID uuid, final String platform, final int seconds) {
        if (seconds <= 0) {
            expiry.remove(key(uuid, platform));
            return;
        }
        expiry.put(key(uuid, platform), System.currentTimeMillis() + (seconds * 1000L));
    }

    public void clear() {
        expiry.clear();
    }

    private static String key(final UUID uuid, final String platform) {
        return uuid + ":" + platform;
    }
}
