package com.aquaticstudios.aqualive.platforms.bukkit;

import com.aquaticstudios.aqualive.shared.platform.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Level;

public final class BukkitScheduler implements Scheduler {
    private static final String GLOBAL_REGION_SCHEDULER =
            "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler";

    private final Plugin plugin;

    private final Object foliaScheduler;
    private final Method foliaExecute;

    public BukkitScheduler(final Plugin plugin) {
        this.plugin = plugin;

        Object scheduler = null;
        Method execute = null;

        if (BukkitDetector.isFolia()) {
            try {
                scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                execute = Class.forName(GLOBAL_REGION_SCHEDULER)
                        .getMethod("execute", Plugin.class, Runnable.class);
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.SEVERE,
                        "Folia detected but its scheduler could not be reached; "
                                + "announcements will run on the calling thread", ex);
                scheduler = null;
                execute = null;
            }
        }

        this.foliaScheduler = scheduler;
        this.foliaExecute = execute;
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
}
