package com.aquaticstudios.aqualive.platforms.bungeecord;

import com.aquaticstudios.aqualive.platforms.bungeecord.command.BungeeCommands;
import com.aquaticstudios.aqualive.shared.AquaLive;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.io.IOException;
import java.util.logging.Level;

public final class BungeeAquaLive extends Plugin {
    private static final int BSTATS_ID = 32629;

    private BungeeAudiences audiences;
    private AquaLive core;

    @Override
    public void onEnable() {
        audiences = BungeeAudiences.create(this);

        try {
            core = AquaLive.start(new BungeePlatform(this, audiences));
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not read the configuration; AquaLive will stay idle", ex);
            return;
        }

        BungeeCommands.register(this, core, audiences);
        ProxyServer.getInstance().getPluginManager()
                .registerListener(this, new BungeeEventListener(core, audiences));
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
