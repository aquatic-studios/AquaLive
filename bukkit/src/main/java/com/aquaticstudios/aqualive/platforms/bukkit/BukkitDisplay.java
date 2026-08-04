package com.aquaticstudios.aqualive.platforms.bukkit;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public final class BukkitDisplay {
    private static final int DEFAULT_FADE_IN = 10;
    private static final int DEFAULT_STAY = 70;
    private static final int DEFAULT_FADE_OUT = 20;

    private static final Method SEND_TITLE = sendTitleMethod();
    private static final boolean HEX_COLORS = supportsHexColors();
    private static final BungeeComponentSerializer COMPONENTS = componentSerializer();
    private static final LegacyComponentSerializer LEGACY = legacySerializer();

    private static final Map<BossBar, org.bukkit.boss.BossBar> BARS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    public static boolean sendMessage(final CommandSender sender, final Component message) {
        final BaseComponent[] components = components(message);
        if (components == null) return false;

        try {
            sender.spigot().sendMessage(components);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean showTitle(final Player player, final Title title) {
        if (SEND_TITLE == null) return false;

        final Title.Times times = title.times();
        try {
            SEND_TITLE.invoke(player,
                    legacy(title.title()),
                    legacy(title.subtitle()),
                    ticks(times == null ? null : times.fadeIn(), DEFAULT_FADE_IN),
                    ticks(times == null ? null : times.stay(), DEFAULT_STAY),
                    ticks(times == null ? null : times.fadeOut(), DEFAULT_FADE_OUT));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean sendActionBar(final Player player, final Component message) {
        final BaseComponent[] components = components(message);

        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    components != null ? components : TextComponent.fromLegacyText(legacy(message)));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean showBossBar(final Player player, final BossBar bar) {
        try {
            bukkitBar(bar).addPlayer(player);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean hideBossBar(final Player player, final BossBar bar) {
        final org.bukkit.boss.BossBar bukkit = BARS.get(bar);
        if (bukkit == null) return false;

        try {
            bukkit.removePlayer(player);
            if (bukkit.getPlayers().isEmpty()) {
                bukkit.removeAll();
                bukkit.setVisible(false);
                BARS.remove(bar);
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static org.bukkit.boss.BossBar bukkitBar(final BossBar bar) {
        synchronized (BARS) {
            org.bukkit.boss.BossBar bukkit = BARS.get(bar);
            if (bukkit != null) return bukkit;

            bukkit = Bukkit.createBossBar(legacy(bar.name()), color(bar.color()), style(bar.overlay()));
            bukkit.setProgress(Math.max(0d, Math.min(1d, bar.progress())));
            bukkit.setVisible(true);
            BARS.put(bar, bukkit);
            return bukkit;
        }
    }

    private static BarColor color(final BossBar.Color color) {
        try {
            return BarColor.valueOf(color.name());
        } catch (IllegalArgumentException ignored) {
            return BarColor.WHITE;
        }
    }

    private static BarStyle style(final BossBar.Overlay overlay) {
        switch (overlay) {
            case NOTCHED_6:
                return BarStyle.SEGMENTED_6;
            case NOTCHED_10:
                return BarStyle.SEGMENTED_10;
            case NOTCHED_12:
                return BarStyle.SEGMENTED_12;
            case NOTCHED_20:
                return BarStyle.SEGMENTED_20;
            default:
                return BarStyle.SOLID;
        }
    }

    private static BaseComponent[] components(final Component message) {
        if (COMPONENTS == null || message == null) return null;
        try {
            return COMPONENTS.serialize(message);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static BungeeComponentSerializer componentSerializer() {
        try {
            return HEX_COLORS
                    ? BungeeComponentSerializer.get()
                    : BungeeComponentSerializer.legacy();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static LegacyComponentSerializer legacySerializer() {
        final LegacyComponentSerializer.Builder builder = LegacyComponentSerializer.builder()
                .character('§')
                .hexCharacter('#')
                .useUnusualXRepeatedCharacterHexFormat();

        if (HEX_COLORS) builder.hexColors();
        return builder.build();
    }

    private static boolean supportsHexColors() {
        try {
            final String[] parts = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            final int major = Integer.parseInt(parts[0]);
            final int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return major > 1 || minor >= 16;
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static String legacy(final Component component) {
        return component == null ? "" : LEGACY.serialize(component);
    }

    private static int ticks(final Duration duration, final int fallback) {
        if (duration == null || duration.isNegative()) return fallback;

        final long millis = duration.toMillis();
        return millis == 0L ? 0 : (int) Math.max(1L, millis / 50L);
    }

    private static Method sendTitleMethod() {
        try {
            return Player.class.getMethod("sendTitle",
                    String.class, String.class, int.class, int.class, int.class);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
