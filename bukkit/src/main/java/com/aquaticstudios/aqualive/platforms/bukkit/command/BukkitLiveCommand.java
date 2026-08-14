package com.aquaticstudios.aqualive.platforms.bukkit.command;

import com.aquaticstudios.aqualive.platforms.bukkit.BukkitPlayer;
import com.aquaticstudios.aqualive.shared.AquaLive;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class BukkitLiveCommand implements CommandExecutor, TabCompleter {
    private final AquaLive core;
    private final BukkitAudiences audiences;

    public BukkitLiveCommand(final AquaLive core, final BukkitAudiences audiences) {
        this.core = core;
        this.audiences = audiences;
    }

    @Override
    public boolean onCommand(final CommandSender sender,
                             final Command command,
                             final String label,
                             final String[] args) {
        if (!(sender instanceof Player player)) {
            audiences.sender(sender).sendMessage(
                    Component.text("Only players can announce a stream.", NamedTextColor.RED));
            return true;
        }

        core.live().execute(new BukkitPlayer(player, audiences), label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender,
                                      final Command command,
                                      final String label,
                                      final String[] args) {
        return Collections.emptyList();
    }
}
