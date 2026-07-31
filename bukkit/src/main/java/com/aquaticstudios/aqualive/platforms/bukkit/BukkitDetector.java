package com.aquaticstudios.aqualive.platforms.bukkit;

import com.aquaticstudios.aqualive.shared.platform.PlatformType;
import org.bukkit.Bukkit;

import java.util.Locale;

public final class BukkitDetector {
    public static PlatformType detect() {
        if (isFolia()) return PlatformType.FOLIA;
        if (isPurpur()) return PlatformType.PURPUR;
        if (isPaper()) return PlatformType.PAPER;
        return PlatformType.SPIGOT;
    }

    public static boolean isFolia() {
        return exists("io.papermc.paper.threadedregions.RegionizedServer");
    }

    private static boolean isPurpur() {
        return exists("org.purpurmc.purpur.PurpurConfig")
                || Bukkit.getServer().getName().toLowerCase(Locale.ROOT).contains("purpur");
    }

    private static boolean isPaper() {
        return exists("io.papermc.paper.configuration.Configuration")
                || exists("com.destroystokyo.paper.PaperConfig");
    }

    private static boolean exists(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
