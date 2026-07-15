package com.aquaticstudios.aqualive.shared.hook;

import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;

@FunctionalInterface
public interface PlaceholderHook {
    PlaceholderHook NONE = (user, text) -> text;

    String resolve(AquaPlayer user, String text);
}
