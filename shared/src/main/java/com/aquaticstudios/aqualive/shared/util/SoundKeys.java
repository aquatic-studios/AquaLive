package com.aquaticstudios.aqualive.shared.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.Locale;

public final class SoundKeys {
    public static Sound parse(final String id, final float volume, final float pitch) {
        final Key key = key(id);
        return key == null ? null : Sound.sound(key, Sound.Source.MASTER, volume, pitch);
    }

    public static Key key(final String id) {
        if (id == null || id.trim().isEmpty()) return null;

        final String normalized = id.trim().toLowerCase(Locale.ROOT);

        final String candidate = normalized.indexOf(':') >= 0
                ? normalized
                : normalized.replace('_', '.');

        try {
            return Key.key(candidate);
        } catch (Exception ignored) {
            return null;
        }
    }
}
