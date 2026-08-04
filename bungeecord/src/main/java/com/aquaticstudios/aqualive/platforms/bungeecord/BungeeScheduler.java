package com.aquaticstudios.aqualive.platforms.bungeecord;

import com.aquaticstudios.aqualive.shared.platform.Scheduler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class BungeeScheduler implements Scheduler {
    private final Plugin plugin;

    public BungeeScheduler(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sync(final Runnable task) {
        task.run();
    }

    @Override
    public void later(final Runnable task, final Duration delay) {
        ProxyServer.getInstance().getScheduler()
                .schedule(plugin, task, Math.max(1L, delay.toMillis()), TimeUnit.MILLISECONDS);
    }
}
