package com.aquaticstudios.aqualive.platforms.velocity;

import com.aquaticstudios.aqualive.shared.platform.Scheduler;
import com.velocitypowered.api.proxy.ProxyServer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class VelocityScheduler implements Scheduler {
    private final ProxyServer server;
    private final Object plugin;

    public VelocityScheduler(final ProxyServer server, final Object plugin) {
        this.server = server;
        this.plugin = plugin;
    }

    @Override
    public void sync(final Runnable task) {
        task.run();
    }

    @Override
    public void later(final Runnable task, final Duration delay) {
        server.getScheduler()
                .buildTask(plugin, task)
                .delay(Math.max(1L, delay.toMillis()), TimeUnit.MILLISECONDS)
                .schedule();
    }
}
