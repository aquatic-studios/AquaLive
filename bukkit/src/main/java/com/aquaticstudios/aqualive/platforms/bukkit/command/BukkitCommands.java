package com.aquaticstudios.aqualive.platforms.bukkit.command;

import com.aquaticstudios.aqualive.shared.AquaLive;
import com.aquaticstudios.aqualive.shared.platform.CommandSync;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public final class BukkitCommands implements CommandSync {
    private final JavaPlugin plugin;
    private final AquaLive core;
    private final BukkitAudiences audiences;
    private final CommandMap map;

    private final List<PluginCommand> live = new ArrayList<>();

    private BukkitCommands(final JavaPlugin plugin,
                           final AquaLive core,
                           final BukkitAudiences audiences,
                           final CommandMap map) {
        this.plugin = plugin;
        this.core = core;
        this.audiences = audiences;
        this.map = map;
    }

    public static void register(final JavaPlugin plugin, final AquaLive core, final BukkitAudiences audiences) {
        final CommandMap map = commandMap();
        if (map == null) {
            plugin.getLogger().severe("Could not reach Bukkit's command map; no commands were registered.");
            return;
        }

        final BukkitCommands commands = new BukkitCommands(plugin, core, audiences, map);
        commands.create("aqualive", new BukkitAquaLiveCommand(core, audiences));
        commands.sync(core.settings().aliases());
        core.commandSync(commands);
    }

    @Override
    public void sync(final List<String> aliases) {
        for (final PluginCommand command : live) {
            drop(command);
        }
        live.clear();

        for (final String alias : aliases) {
            final PluginCommand command = create(alias, new BukkitLiveCommand(core, audiences));
            if (command != null) live.add(command);
        }
        refreshClients();
    }

    private PluginCommand create(final String name, final CommandExecutor executor) {
        final String fallback = plugin.getName().toLowerCase(Locale.ROOT);
        try {
            final Constructor<PluginCommand> constructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            final PluginCommand command = constructor.newInstance(name, plugin);
            command.setExecutor(executor);
            if (executor instanceof TabCompleter completer) {
                command.setTabCompleter(completer);
            }

            if (!map.register(fallback, command)) {
                plugin.getLogger().warning("/" + name + " is already taken by another plugin, "
                        + "AquaLive registered it as /" + fallback + ":" + name);
            }
            return command;
        } catch (Throwable ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not register command /" + name, ex);
            return null;
        }
    }

    private void drop(final PluginCommand command) {
        try {
            command.unregister(map);

            final Map<String, Command> known = knownCommands();
            if (known != null) known.values().removeIf(entry -> entry == command);
        } catch (Throwable ex) {
            plugin.getLogger().log(Level.WARNING, "Could not unregister command /" + command.getName(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> knownCommands() {
        if (!(map instanceof SimpleCommandMap)) return null;

        Object commands = read(() -> SimpleCommandMap.class.getMethod("getKnownCommands").invoke(map));
        if (commands == null) {
            commands = read(() -> {
                final Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
                field.setAccessible(true);
                return field.get(map);
            });
        }
        return commands instanceof Map ? (Map<String, Command>) commands : null;
    }

    private void refreshClients() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.updateCommands();
            } catch (Throwable unsupported) {
                return;
            }
        }
    }

    private static CommandMap commandMap() {
        Object found = read(() -> Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer()));
        if (found == null) {
            found = read(() -> {
                final Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                field.setAccessible(true);
                return field.get(Bukkit.getServer());
            });
        }
        return found instanceof CommandMap map ? map : null;
    }

    private static Object read(final Callable<Object> source) {
        try {
            return source.call();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
