package com.aquaticstudios.aqualive.platforms.velocity;

import com.aquaticstudios.aqualive.shared.platform.Sender;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.Audience;

public final class VelocitySender implements Sender {
    private final CommandSource source;
    private final Audience audience;

    public VelocitySender(final CommandSource source) {
        this.source = source;
        this.audience = new VelocityAudience(source);
    }

    @Override
    public String name() {
        return (source instanceof Player player) ? player.getUsername() : "CONSOLE";
    }

    @Override
    public boolean hasPermission(final String permission) {
        return permission == null || permission.isEmpty() || source.hasPermission(permission);
    }

    @Override
    public Audience audience() {
        return audience;
    }
}
