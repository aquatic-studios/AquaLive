package com.aquaticstudios.aqualive.platforms.bukkit;

import com.aquaticstudios.aqualive.platforms.bukkit.hook.PlaceholderApiHook;
import com.aquaticstudios.aqualive.shared.hook.PlaceholderHook;
import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import com.aquaticstudios.aqualive.shared.platform.JavaLogger;
import com.aquaticstudios.aqualive.shared.platform.Platform;
import com.aquaticstudios.aqualive.shared.platform.PlatformType;
import com.aquaticstudios.aqualive.shared.platform.PluginLogger;
import com.aquaticstudios.aqualive.shared.platform.Scheduler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public final class BukkitPlatform implements Platform {
    private final JavaPlugin plugin;
    private final BukkitAudiences audiences;
    private final PlatformType type;
    private final Scheduler scheduler;
    private final PlaceholderHook placeholders;

    public BukkitPlatform(final JavaPlugin plugin, final BukkitAudiences audiences) {
        this.plugin = plugin;
        this.audiences = audiences;
        this.type = BukkitDetector.detect();
        this.scheduler = new BukkitScheduler(plugin);

        this.placeholders = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
                ? new PlaceholderApiHook()
                : PlaceholderHook.NONE;
    }

    @Override
    public PlatformType type() {
        return type;
    }

    @Override
    public Collection<? extends AquaPlayer> onlinePlayers() {
        final Collection<AquaPlayer> users = new ArrayList<>();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            users.add(new BukkitPlayer(player, audiences));
        }
        return users;
    }

    @Override
    public int maxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public PluginLogger logger() {
        return new JavaLogger(plugin.getLogger());
    }

    @Override
    public Path dataFolder() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public PlaceholderHook placeholders() {
        return placeholders;
    }
}
