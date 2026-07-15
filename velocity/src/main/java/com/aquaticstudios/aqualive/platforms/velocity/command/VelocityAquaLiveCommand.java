package com.aquaticstudios.aqualive.platforms.velocity.command;

import com.aquaticstudios.aqualive.platforms.velocity.VelocitySender;
import com.aquaticstudios.aqualive.shared.AquaLive;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class VelocityAquaLiveCommand implements SimpleCommand {
    private final AquaLive core;

    public VelocityAquaLiveCommand(final AquaLive core) {
        this.core = core;
    }

    @Override
    public void execute(final Invocation invocation) {
        core.admin().execute(new VelocitySender(invocation.source()), invocation.arguments());
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        final String[] args = invocation.arguments();
        if (args.length > 1) return Collections.emptyList();

        final String typed = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        final List<String> out = new ArrayList<>();
        for (final String sub : core.admin().subCommands()) {
            if (sub.startsWith(typed)) out.add(sub);
        }
        return out;
    }
}
