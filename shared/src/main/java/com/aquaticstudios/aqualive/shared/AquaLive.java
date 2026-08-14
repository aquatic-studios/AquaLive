package com.aquaticstudios.aqualive.shared;

import com.aquaticstudios.aqualive.shared.command.AquaLiveCommand;
import com.aquaticstudios.aqualive.shared.command.LiveCommand;
import com.aquaticstudios.aqualive.shared.config.ConfigAudit;
import com.aquaticstudios.aqualive.shared.config.Messages;
import com.aquaticstudios.aqualive.shared.config.Platforms;
import com.aquaticstudios.aqualive.shared.config.Settings;
import com.aquaticstudios.aqualive.shared.config.Webhook;
import com.aquaticstudios.aqualive.shared.features.Announcer;
import com.aquaticstudios.aqualive.shared.features.Cooldowns;
import com.aquaticstudios.aqualive.shared.features.WebhookSender;
import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import com.aquaticstudios.aqualive.shared.platform.CommandSync;
import com.aquaticstudios.aqualive.shared.platform.Platform;
import com.github.senkex.headrender.HeadRender;

import java.io.IOException;

public final class AquaLive {
    private final Platform platform;
    private final Settings settings;
    private final Messages messages;
    private final Platforms platforms;
    private final Webhook webhook;
    private final WebhookSender webhookSender;
    private final Cooldowns cooldowns = new Cooldowns();

    private CommandSync commandSync;

    private final LiveCommand live;
    private final AquaLiveCommand admin;

    private AquaLive(final Platform platform,
                     final Settings settings,
                     final Messages messages,
                     final Platforms platforms,
                     final Webhook webhook) {
        this.platform = platform;
        this.settings = settings;
        this.messages = messages;
        this.platforms = platforms;
        this.webhook = webhook;

        this.webhookSender = new WebhookSender(platform, webhook);
        final Announcer broadcast = new Announcer(platform, settings, messages, platforms, webhookSender);
        this.live = new LiveCommand(platform, settings, messages, cooldowns, broadcast);
        this.admin = new AquaLiveCommand(this, settings, messages);
    }

    public static AquaLive start(final Platform platform) throws IOException {
        final Settings settings = Settings.load(platform.dataFolder());
        final Messages messages = Messages.load(platform.dataFolder());
        final Platforms platforms = Platforms.load(platform.dataFolder());
        final Webhook webhook = Webhook.load(platform.dataFolder());

        final AquaLive core = new AquaLive(platform, settings, messages, platforms, webhook);
        platform.logger().info("AquaLive enabled on " + platform.type().displayName());
        core.audit();
        return core;
    }

    public ConfigAudit reload() throws IOException {
        settings.reload();
        messages.reload();
        platforms.reload();
        webhook.reload();
        webhookSender.resetWarnings();
        cooldowns.clear();

        HeadRender.clearCache();
        if (commandSync != null) commandSync.sync(settings.aliases());
        return audit();
    }

    private ConfigAudit audit() {
        final ConfigAudit audit = ConfigAudit.run(settings, platforms, webhook);

        platform.logger().info("Loaded " + settings.platforms().size() + " platform(s): "
                + String.join(", ", settings.platforms().keySet()));

        if (audit.isClean()) return audit;

        platform.logger().warn("Found " + audit.problems().size() + " problem(s) in the AquaLive configuration:");
        for (final String problem : audit.problems()) {
            platform.logger().warn(" - " + problem);
        }
        return audit;
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

    public void commandSync(final CommandSync sync) {
        this.commandSync = sync;
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
