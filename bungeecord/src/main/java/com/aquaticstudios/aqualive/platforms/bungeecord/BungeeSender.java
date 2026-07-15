package com.aquaticstudios.aqualive.platforms.bungeecord;

import com.aquaticstudios.aqualive.shared.platform.Sender;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;

public final class BungeeSender implements Sender {
    private final CommandSender sender;
    private final BungeeAudiences audiences;

    public BungeeSender(final CommandSender sender, final BungeeAudiences audiences) {
        this.sender = sender;
        this.audiences = audiences;
    }

    @Override
    public String name() {
        return sender.getName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }

    @Override
    public Audience audience() {
        return audiences.sender(sender);
    }
}
