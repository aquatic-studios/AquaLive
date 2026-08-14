package com.aquaticstudios.aqualive.shared.platform;

import java.util.List;

@FunctionalInterface
public interface CommandSync {
    void sync(List<String> aliases);
}
