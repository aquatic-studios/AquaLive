package com.aquaticstudios.aqualive.shared.platform;

import java.util.UUID;

public interface AquaPlayer extends Sender {
    UUID uuid();

    String serverName();
}
