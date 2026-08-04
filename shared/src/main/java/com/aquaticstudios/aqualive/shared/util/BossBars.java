package com.aquaticstudios.aqualive.shared.util;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

import java.util.Locale;

public final class BossBars {
    public static BossBar create(final Component name,
                                 final String color,
                                 final String overlay,
                                 final double progress) {
        return BossBar.bossBar(name, clamp(progress), color(color), overlay(overlay));
    }

    public static BossBar.Color color(final String id) {
        if (id == null) return BossBar.Color.WHITE;
        try {
            return BossBar.Color.valueOf(id.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return BossBar.Color.WHITE;
        }
    }

    public static BossBar.Overlay overlay(final String id) {
        if (id == null) return BossBar.Overlay.PROGRESS;

        final String normalized = id.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        final String candidate = "SOLID".equals(normalized)
                ? "PROGRESS"
                : normalized.startsWith("SEGMENTED_") ? normalized.replace("SEGMENTED_", "NOTCHED_") : normalized;

        try {
            return BossBar.Overlay.valueOf(candidate);
        } catch (IllegalArgumentException ignored) {
            return BossBar.Overlay.PROGRESS;
        }
    }

    private static float clamp(final double progress) {
        if (progress < 0) return 0f;
        if (progress > 1) return 1f;
        return (float) progress;
    }
}
