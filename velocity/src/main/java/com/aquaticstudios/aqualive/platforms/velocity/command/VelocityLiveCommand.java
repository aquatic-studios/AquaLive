package com.aquaticstudios.aqualive.platforms.velocity.command;

import com.aquaticstudios.aqualive.platforms.velocity.VelocityPlayer;
import com.aquaticstudios.aqualive.platforms.velocity.VelocitySender;
import com.aquaticstudios.aqualive.shared.AquaLive;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class VelocityLiveCommand implements SimpleCommand {
    private final AquaLive core;

    public VelocityLiveCommand(final AquaLive core) {
        this.core = core;
    }

    @Override
    public void execute(final Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            new VelocitySender(invocation.source()).sendMessage(
                    Component.text("Only players can announce a stream.", NamedTextColor.RED));
            return;
        }
        core.live().execute(new VelocityPlayer(player), invocation.alias(), invocation.arguments());
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        final String[] args = invocation.arguments();
        if (args.length > 1) return Collections.emptyList();

        final String typed = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        final List<String> out = new ArrayList<>();
        for (final String domain : core.live().domains()) {
            if (domain.startsWith(typed)) out.add(domain);
        }
        return out;
    }
}
