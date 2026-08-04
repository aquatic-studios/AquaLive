package com.aquaticstudios.aqualive.shared.command;

import com.aquaticstudios.aqualive.shared.AquaLive;
import com.aquaticstudios.aqualive.shared.BuildInfo;
import com.aquaticstudios.aqualive.shared.chat.ChatRenderer;
import com.aquaticstudios.aqualive.shared.config.Messages;
import com.aquaticstudios.aqualive.shared.config.Settings;
import com.aquaticstudios.aqualive.shared.config.StreamPlatform;
import com.aquaticstudios.aqualive.shared.placeholders.Placeholders;
import com.aquaticstudios.aqualive.shared.platform.Sender;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class AquaLiveCommand {
    private static final List<String> SUB_COMMANDS = Arrays.asList("reload", "platforms", "help");

    private final AquaLive core;
    private final Settings settings;
    private final Messages messages;

    public AquaLiveCommand(final AquaLive core, final Settings settings, final Messages messages) {
        this.core = core;
        this.settings = settings;
        this.messages = messages;
    }

    public void execute(final Sender sender, final String[] args) {
        if (args.length == 0) {
            banner(sender);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> help(sender);
            case "reload" -> reload(sender);
            case "platforms" -> platforms(sender);
            default -> send(sender, "unknown-command");
        }
    }

    public List<String> subCommands() {
        return SUB_COMMANDS;
    }

    private void banner(final Sender sender) {
        final Placeholders placeholders = Placeholders.create();
        final String[] lines = {
                " ",
                "            &fPowered by &#8BD5FFSenkex @ Aquatic Studios",
                "     &#54ADF4&lAquaLive &fversion &#8DFF87[" + BuildInfo.VERSION + "] &f| Platform: &7(" + core.platform().type().displayName() + ")",
                " "
        };
        for (final String line : lines) {
            sender.sendMessage(ChatRenderer.text(line, placeholders));
        }
    }

    private void reload(final Sender sender) {
        if (!sender.hasPermission(settings.adminPermission())) {
            send(sender, "no-permission");
            return;
        }
        try {
            core.reload();
            send(sender, "reload");
        } catch (IOException ex) {
            sender.sendMessage(ChatRenderer.text(
                    "&cReload failed: " + ex.getMessage(), Placeholders.create()));
        }
    }

    private void help(final Sender sender) {
        if (!sender.hasPermission(settings.adminPermission())) {
            send(sender, "no-permission");
            return;
        }
        for (final String line : messages.list("messages.help")) {
            sender.sendMessage(ChatRenderer.text(line, Placeholders.create()));
        }
    }

    private void platforms(final Sender sender) {
        if (!sender.hasPermission(settings.adminPermission())) {
            send(sender, "no-permission");
            return;
        }
        for (final String line : messages.list("messages.platforms")) {
            if (!line.contains("%platform_name%")) {
                sender.sendMessage(ChatRenderer.text(line, Placeholders.create()));
                continue;
            }
            for (final StreamPlatform platform : settings.platforms().values()) {
                sender.sendMessage(ChatRenderer.text(line, Placeholders.create()
                        .set("platform_name", platform.id())
                        .set("platform_domain", platform.domain())
                        .set("platform_permission", platform.permission() == null ? "-" : platform.permission())
                        .set("platform_cooldown", String.valueOf(platform.cooldown()))));
            }
        }
    }

    private void send(final Sender to, final String path) {
        final String raw = messages.get(path);
        if (raw.isEmpty()) return;
        to.sendMessage(ChatRenderer.text(raw, Placeholders.create()));
    }
}
