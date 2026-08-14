package com.aquaticstudios.aqualive.shared.features;

import com.aquaticstudios.aqualive.shared.chat.Buttons;
import com.aquaticstudios.aqualive.shared.chat.ChatRenderer;
import com.aquaticstudios.aqualive.shared.config.Messages;
import com.aquaticstudios.aqualive.shared.config.Platforms;
import com.aquaticstudios.aqualive.shared.config.Settings;
import com.aquaticstudios.aqualive.shared.config.StreamPlatform;
import com.aquaticstudios.aqualive.shared.config.YamlFile;
import com.aquaticstudios.aqualive.shared.features.layout.ClassicLayout;
import com.aquaticstudios.aqualive.shared.features.layout.HeadLayout;
import com.aquaticstudios.aqualive.shared.placeholders.Placeholders;
import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import com.aquaticstudios.aqualive.shared.platform.Platform;
import com.aquaticstudios.aqualive.shared.util.BossBars;
import com.aquaticstudios.aqualive.shared.util.SoundKeys;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class Announcer {
    private static final String LAYOUT_HEAD = "head";
    private static final Duration ACTION_BAR_STEP = Duration.ofSeconds(1);

    private final Platform platform;
    private final Settings settings;
    private final Messages messages;
    private final Platforms platforms;
    private final WebhookSender webhook;

    public Announcer(final Platform platform,
                     final Settings settings,
                     final Messages messages,
                     final Platforms platforms,
                     final WebhookSender webhook) {
        this.platform = platform;
        this.settings = settings;
        this.messages = messages;
        this.platforms = platforms;
        this.webhook = webhook;
    }

    public void broadcast(final AquaPlayer streamer, final StreamPlatform stream, final String url) {
        final YamlFile yaml = platforms.yaml();
        final String section = "advertisement." + stream.id();

        if (!yaml.contains(section)) {
            platform.logger().warn("platforms.yml has no '" + section + "' block, nothing to announce for '"
                    + stream.id() + "'");
            return;
        }
        if (!yaml.getBoolean(section + ".enabled", true)) return;

        final String streamerServer = streamer.serverName();

        final boolean serverClickAllowed = platform.type().isProxy()
                && streamerServer != null
                && !settings.isBlacklisted(streamerServer);

        final Placeholders placeholders = placeholders(streamer, stream, url);
        final Map<String, Component> buttons = buttons(yaml, stream, placeholders, serverClickAllowed);

        if (yaml.getBoolean(section + ".broadcast.chat.enabled", true)) {
            sendChat(yaml, section, placeholders, buttons);
        }
        sendTitle(yaml, section, placeholders);
        sendActionBar(yaml, section, placeholders);
        sendBossBar(yaml, section, placeholders);
        sendSound(yaml, section);
        webhook.send(stream, placeholders);
    }

    private void sendChat(final YamlFile yaml,
                          final String section,
                          final Placeholders placeholders,
                          final Map<String, Component> buttons) {
        final String layout = yaml.getString(section + ".broadcast.chat.layout", LAYOUT_HEAD);

        if (!LAYOUT_HEAD.equalsIgnoreCase(layout)) {
            dispatch(ClassicLayout.build(yaml, section, placeholders, buttons));
            return;
        }

        final String problem = HeadLayout.validate(yaml, section);
        if (problem != null) {
            platform.logger().warn("Head layout unusable, falling back to classic: " + problem);
            dispatch(ClassicLayout.build(yaml, section, placeholders, buttons));
            return;
        }

        HeadLayout.build(yaml, section, placeholders, buttons)
                .whenComplete((lines, error) -> {
                    if (error != null || lines == null || lines.isEmpty()) {
                        platform.logger().error(
                                "Could not render the head, falling back to classic", error);
                        dispatch(ClassicLayout.build(yaml, section, placeholders, buttons));
                        return;
                    }
                    dispatch(lines);
                });
    }

    private void sendSound(final YamlFile yaml, final String section) {
        if (!yaml.getBoolean(section + ".broadcast.sound.enabled", true)) return;

        final Sound sound = SoundKeys.parse(
                yaml.getString(section + ".broadcast.sound.id", null),
                (float) yaml.getInt(section + ".broadcast.sound.volume", 1),
                (float) yaml.getInt(section + ".broadcast.sound.pitch", 1));

        if (sound == null) return;
        each(user -> user.playSound(sound));
    }

    private void sendTitle(final YamlFile yaml, final String section, final Placeholders placeholders) {
        if (!yaml.getBoolean(section + ".broadcast.title.enabled", true)) return;

        final Title title = Title.title(
                ChatRenderer.text(yaml.getString(section + ".broadcast.title.up", ""), placeholders),
                ChatRenderer.text(yaml.getString(section + ".broadcast.title.down", ""), placeholders),
                Title.Times.times(
                        Duration.ofMillis(yaml.getInt(section + ".broadcast.title.fade-in", 500)),
                        Duration.ofMillis(yaml.getInt(section + ".broadcast.title.stay", 3000)),
                        Duration.ofMillis(yaml.getInt(section + ".broadcast.title.fade-out", 500))));

        each(user -> user.showTitle(title));
    }

    private void sendActionBar(final YamlFile yaml, final String section, final Placeholders placeholders) {
        if (!yaml.getBoolean(section + ".broadcast.actionbar.enabled", true)) return;

        final String raw = yaml.getString(section + ".broadcast.actionbar.text", "");
        if (raw.isEmpty()) return;

        final Component text = ChatRenderer.text(raw, placeholders);
        repeatActionBar(text, Math.max(1, yaml.getInt(section + ".broadcast.actionbar.duration", 5)));
    }

    private void repeatActionBar(final Component text, final int remaining) {
        each(user -> user.sendActionBar(text));
        if (remaining <= 1) return;
        platform.scheduler().later(() -> repeatActionBar(text, remaining - 1), ACTION_BAR_STEP);
    }

    private void sendBossBar(final YamlFile yaml, final String section, final Placeholders placeholders) {
        if (!yaml.getBoolean(section + ".broadcast.bossbar.enabled", true)) return;

        final String raw = yaml.getString(section + ".broadcast.bossbar.text", "");
        if (raw.isEmpty()) return;

        final BossBar bar = BossBars.create(
                ChatRenderer.text(raw, placeholders),
                yaml.getString(section + ".broadcast.bossbar.color", "WHITE"),
                yaml.getString(section + ".broadcast.bossbar.style", "PROGRESS"),
                yaml.getDouble(section + ".broadcast.bossbar.progress", 1.0));

        final int seconds = Math.max(1, yaml.getInt(section + ".broadcast.bossbar.duration", 8));
        final List<AquaPlayer> viewers = recipients();

        platform.scheduler().sync(() -> deliver(viewers, user -> user.showBossBar(bar)));

        platform.scheduler().later(
                () -> platform.scheduler().sync(() -> deliver(viewers, user -> user.hideBossBar(bar))),
                Duration.ofSeconds(seconds));
    }

    private void dispatch(final List<Component> lines) {
        if (lines.isEmpty()) return;
        each(user -> {
            for (final Component line : lines) {
                user.sendMessage(line);
            }
        });
    }

    private void each(final Consumer<AquaPlayer> action) {
        platform.scheduler().sync(() -> deliver(recipients(), action));
    }

    private void deliver(final List<AquaPlayer> users, final Consumer<AquaPlayer> action) {
        for (final AquaPlayer user : users) {
            try {
                action.accept(user);
            } catch (Throwable error) {
                platform.logger().error("Could not deliver an announcement to " + user.name(), error);
            }
        }
    }

    private List<AquaPlayer> recipients() {
        final List<AquaPlayer> out = new ArrayList<>();
        for (final AquaPlayer user : platform.onlinePlayers()) {
            if (settings.isBlacklisted(user.serverName())) continue;
            out.add(user);
        }
        return out;
    }

    private Placeholders placeholders(final AquaPlayer streamer, final StreamPlatform stream, final String url) {
        return Placeholders.create()
                .set("prefix", messages.prefix())
                .set("player", streamer.name())
                .set("url", url)
                .set("platform_id", stream.id())
                .set("platform", platform.type().displayName())
                .set("server", serverDisplay(streamer))
                .set("server_online", String.valueOf(platform.onlinePlayers().size()))
                .set("server_max_players", String.valueOf(platform.maxPlayers()))
                .resolver(platform.placeholders(), streamer);
    }

    private String serverDisplay(final AquaPlayer streamer) {
        if (platform.type().isProxy()) {
            final String server = streamer.serverName();
            if (server != null) return server;
        }
        final String configured = settings.serverName();
        return (configured == null || configured.isEmpty())
                ? platform.type().displayName()
                : configured;
    }

    private Map<String, Component> buttons(final YamlFile yaml,
                                           final StreamPlatform stream,
                                           final Placeholders placeholders,
                                           final boolean serverClickAllowed) {
        final Map<String, Component> out = new LinkedHashMap<>();
        for (final String id : yaml.getKeys("buttons")) {
            out.put("%button_" + id + "%",
                    Buttons.build(yaml, id, placeholders, serverClickAllowed));
        }

        final String own = "%button_" + stream.id() + "%";
        if (!out.containsKey(own)) {
            out.put(own, Buttons.build(yaml, "link", placeholders, serverClickAllowed));
        }
        return out;
    }
}
