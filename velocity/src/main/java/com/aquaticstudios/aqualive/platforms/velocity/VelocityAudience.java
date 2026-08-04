package com.aquaticstudios.aqualive.platforms.velocity;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

final class VelocityAudience implements Audience {
    private final Object target;

    VelocityAudience(final Object target) {
        this.target = target;
    }

    @Override
    public void sendMessage(final Component message) {
        VelocityBridge.sendMessage(target, message);
    }

    @Override
    public void sendActionBar(final Component message) {
        VelocityBridge.sendActionBar(target, message);
    }

    @Override
    public void showTitle(final Title title) {
        VelocityBridge.showTitle(target, title);
    }

    @Override
    public void playSound(final Sound sound) {
        VelocityBridge.playSound(target, sound);
    }

    @Override
    public void showBossBar(final BossBar bar) {
        VelocityBridge.showBossBar(target, bar);
    }

    @Override
    public void hideBossBar(final BossBar bar) {
        VelocityBridge.hideBossBar(target, bar);
    }
}
