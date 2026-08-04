package com.aquaticstudios.aqualive.platforms.bukkit;

import com.aquaticstudios.aqualive.shared.platform.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class BukkitScheduler implements Scheduler {
    private static final String GLOBAL_REGION_SCHEDULER =
            "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler";

    private final Plugin plugin;

    private final Object foliaScheduler;
    private final Method foliaExecute;
    private final Method foliaRunDelayed;

    public BukkitScheduler(final Plugin plugin) {
        this.plugin = plugin;

        Object scheduler = null;
        Method execute = null;
        Method runDelayed = null;

        if (BukkitDetector.isFolia()) {
            try {
                scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                final Class<?> type = Class.forName(GLOBAL_REGION_SCHEDULER);
                execute = type.getMethod("execute", Plugin.class, Runnable.class);
                runDelayed = type.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.SEVERE,
                        "Folia detected but its scheduler could not be reached; "
                                + "announcements will run on the calling thread", ex);
                scheduler = null;
                execute = null;
                runDelayed = null;
            }
        }

        this.foliaScheduler = scheduler;
        this.foliaExecute = execute;
        this.foliaRunDelayed = runDelayed;
    }

    @Override
    public void sync(final Runnable task) {
        if (foliaScheduler != null) {
            try {
                foliaExecute.invoke(foliaScheduler, plugin, task);
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.WARNING, "Folia scheduler rejected a task", ex);
            }
            return;
        }

        if (Bukkit.isPrimaryThread()) {
            task.run();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void later(final Runnable task, final Duration delay) {
        final long ticks = Math.max(1L, delay.toMillis() / 50L);

        if (foliaScheduler != null && foliaRunDelayed != null) {
            try {
                final Consumer<Object> wrapper = ignored -> task.run();
                foliaRunDelayed.invoke(foliaScheduler, plugin, wrapper, ticks);
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.WARNING, "Folia scheduler rejected a delayed task", ex);
            }
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
    }
}
