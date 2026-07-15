package com.aquaticstudios.aqualive.platforms.bukkit;

import com.aquaticstudios.aqualive.platforms.bukkit.command.BukkitCommands;
import com.aquaticstudios.aqualive.shared.AquaLive;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public final class BukkitAquaLive extends JavaPlugin {
    private static final int BSTATS_ID = 32628;

    private BukkitAudiences audiences;
    private AquaLive core;

    @Override
    public void onEnable() {
        audiences = BukkitAudiences.create(this);

        try {
            core = AquaLive.start(new BukkitPlatform(this, audiences));
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not read the configuration; disabling AquaLive", ex);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        BukkitCommands.register(this, core, audiences);
        getServer().getPluginManager().registerEvents(new BukkitEventListener(core, audiences), this);
        new Metrics(this, BSTATS_ID);
    }

    @Override
    public void onDisable() {
        if (core != null) core.stop();
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
    }
}
