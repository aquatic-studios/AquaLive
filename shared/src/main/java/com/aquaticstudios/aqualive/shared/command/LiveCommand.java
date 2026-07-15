package com.aquaticstudios.aqualive.shared.command;

import com.aquaticstudios.aqualive.shared.chat.ChatRenderer;
import com.aquaticstudios.aqualive.shared.config.Messages;
import com.aquaticstudios.aqualive.shared.config.Settings;
import com.aquaticstudios.aqualive.shared.config.StreamPlatform;
import com.aquaticstudios.aqualive.shared.features.Announcer;
import com.aquaticstudios.aqualive.shared.features.Cooldowns;
import com.aquaticstudios.aqualive.shared.placeholders.Placeholders;
import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import com.aquaticstudios.aqualive.shared.platform.Platform;
import com.aquaticstudios.aqualive.shared.platform.Sender;
import com.aquaticstudios.aqualive.shared.util.Urls;

import java.util.List;

public final class LiveCommand {
    private final Platform platform;
    private final Settings settings;
    private final Messages messages;
    private final Cooldowns cooldowns;
    private final Announcer broadcast;

    public LiveCommand(final Platform platform,
                       final Settings settings,
                       final Messages messages,
                       final Cooldowns cooldowns,
                       final Announcer broadcast) {
        this.platform = platform;
        this.settings = settings;
        this.messages = messages;
        this.cooldowns = cooldowns;
        this.broadcast = broadcast;
    }

    public void execute(final AquaPlayer user, final String label, final String[] args) {
        if (!user.hasPermission(settings.commandPermission())) {
            send(user, "no-permission", Placeholders.create());
            return;
        }

        if (args.length == 0) {
            sendSyntax(user, label);
            return;
        }

        final String url = Urls.normalize(args[0]);
        final StreamPlatform stream = settings.platformForDomain(Urls.domain(url));

        if (stream == null) {
            sendSyntax(user, label);
            return;
        }

        if (stream.permission() != null && !user.hasPermission(stream.permission())) {
            send(user, "platforms-no-permission",
                    Placeholders.create().set("platform_permission", stream.permission()));
            return;
        }

        if (platform.type().isProxy() && settings.isBlacklisted(user.serverName())) {
            send(user, "blacklisted-server",
                    Placeholders.create().set("server", String.valueOf(user.serverName())));
            return;
        }

        if (!user.hasPermission(settings.bypassCooldownPermission())) {
            final int remaining = cooldowns.remaining(user.uuid(), stream.id());
            if (remaining > 0) {
                send(user, "cooldown", Placeholders.create().set("cooldown", String.valueOf(remaining)));
                return;
            }
            cooldowns.start(user.uuid(), stream.id(), stream.cooldown());
        }

        broadcast.broadcast(user, stream, url);
    }

    public List<String> domains() {
        return settings.platforms().values().stream().map(StreamPlatform::domain).toList();
    }

    private void sendSyntax(final Sender to, final String label) {
        final Placeholders placeholders = Placeholders.create()
                .set("aliases", label)
                .set("domain", "<url>")
                .set("platforms_available", String.join(", ", settings.platforms().keySet()));

        for (final String line : messages.list("syntax.aliases.error")) {
            to.audience().sendMessage(ChatRenderer.text(line, placeholders));
        }
    }

    private void send(final Sender to, final String path, final Placeholders placeholders) {
        final String raw = messages.get(path);
        if (raw.isEmpty()) return;
        to.audience().sendMessage(ChatRenderer.text(raw, placeholders));
    }
}
