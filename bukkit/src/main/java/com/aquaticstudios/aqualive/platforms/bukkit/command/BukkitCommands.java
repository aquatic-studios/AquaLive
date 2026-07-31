package com.aquaticstudios.aqualive.platforms.bukkit.command;

import com.aquaticstudios.aqualive.shared.AquaLive;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.logging.Level;

public final class BukkitCommands {
    public static void register(final JavaPlugin plugin, final AquaLive core, final BukkitAudiences audiences) {
        final CommandMap map = commandMap();
        if (map == null) {
            plugin.getLogger().severe("Could not reach Bukkit's command map; no commands were registered.");
            return;
        }

        register(plugin, map, "aqualive", new BukkitAquaLiveCommand(core, audiences));

        for (final String alias : core.settings().aliases()) {
            register(plugin, map, alias, new BukkitLiveCommand(core, audiences));
        }
    }

    private static CommandMap commandMap() {
        try {
            final Method getter = Bukkit.getServer().getClass().getMethod("getCommandMap");
            return (CommandMap) getter.invoke(Bukkit.getServer());
        } catch (Throwable ignored) {
        }

        try {
            final Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void register(final JavaPlugin plugin,
                                 final CommandMap map,
                                 final String name,
                                 final CommandExecutor executor) {
        try {
            final Constructor<PluginCommand> constructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            final PluginCommand command = constructor.newInstance(name, plugin);
            command.setExecutor(executor);
            if (executor instanceof TabCompleter completer) {
                command.setTabCompleter(completer);
            }

            map.register(plugin.getName().toLowerCase(Locale.ROOT), command);
        } catch (Throwable ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not register command /" + name, ex);
        }
    }
}
