package com.aquaticstudios.aqualive.shared;

import com.aquaticstudios.aqualive.shared.command.AquaLiveCommand;
import com.aquaticstudios.aqualive.shared.command.LiveCommand;
import com.aquaticstudios.aqualive.shared.config.Messages;
import com.aquaticstudios.aqualive.shared.config.Platforms;
import com.aquaticstudios.aqualive.shared.config.Settings;
import com.aquaticstudios.aqualive.shared.features.Announcer;
import com.aquaticstudios.aqualive.shared.features.Cooldowns;
import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import com.aquaticstudios.aqualive.shared.platform.Platform;
import com.github.senkex.headrender.HeadRender;

import java.io.IOException;

public final class AquaLive {
    private final Platform platform;
    private final Settings settings;
    private final Messages messages;
    private final Platforms platforms;
    private final Cooldowns cooldowns = new Cooldowns();

    private final LiveCommand live;
    private final AquaLiveCommand admin;

    private AquaLive(final Platform platform,
                     final Settings settings,
                     final Messages messages,
                     final Platforms platforms) {
        this.platform = platform;
        this.settings = settings;
        this.messages = messages;
        this.platforms = platforms;

        final Announcer broadcast = new Announcer(platform, settings, messages, platforms);
        this.live = new LiveCommand(platform, settings, messages, cooldowns, broadcast);
        this.admin = new AquaLiveCommand(this, settings, messages);
    }

    public static AquaLive start(final Platform platform) throws IOException {
        final Settings settings = Settings.load(platform.dataFolder());
        final Messages messages = Messages.load(platform.dataFolder());
        final Platforms platforms = Platforms.load(platform.dataFolder());

        final AquaLive core = new AquaLive(platform, settings, messages, platforms);
        platform.logger().info("AquaLive enabled on " + platform.type().displayName());
        return core;
    }

    public void reload() throws IOException {
        settings.reload();
        messages.reload();
        platforms.reload();
        cooldowns.clear();

        HeadRender.clearCache();
    }

    public void warmSkin(final AquaPlayer user) {
        if (!settings.preloadSkins()) return;
        if (!user.hasPermission(settings.commandPermission())) return;
        HeadRender.render(user.name()).exceptionally(error -> null);
    }

    public void stop() {
        HeadRender.shutdown();
        platform.logger().info("AquaLive disabled");
    }

    public LiveCommand live() {
        return live;
    }

    public AquaLiveCommand admin() {
        return admin;
    }

    public Settings settings() {
        return settings;
    }

    public Platform platform() {
        return platform;
    }
}
