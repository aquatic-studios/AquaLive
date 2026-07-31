package com.aquaticstudios.aqualive.shared.platform;

import net.kyori.adventure.audience.Audience;

public interface Sender {
    String name();

    boolean hasPermission(String permission);

    Audience audience();
}