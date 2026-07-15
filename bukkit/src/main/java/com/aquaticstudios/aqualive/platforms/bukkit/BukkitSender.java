package com.aquaticstudios.aqualive.platforms.bukkit;

import com.aquaticstudios.aqualive.shared.platform.Sender;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;

public final class BukkitSender implements Sender {
    private final CommandSender sender;
    private final BukkitAudiences audiences;

    public BukkitSender(final CommandSender sender, final BukkitAudiences audiences) {
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
