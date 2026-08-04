package com.aquaticstudios.aqualive.shared.platform;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public interface Sender {
    String name();

    boolean hasPermission(String permission);

    Audience audience();

    default void sendMessage(final Component message) {
        audience().sendMessage(message);
    }

    default void sendActionBar(final Component message) {
        audience().sendActionBar(message);
    }

    default void showTitle(final Title title) {
        audience().showTitle(title);
    }

    default void playSound(final Sound sound) {
        audience().playSound(sound);
    }

    default void showBossBar(final BossBar bar) {
        audience().showBossBar(bar);
    }

    default void hideBossBar(final BossBar bar) {
        audience().hideBossBar(bar);
    }
}
