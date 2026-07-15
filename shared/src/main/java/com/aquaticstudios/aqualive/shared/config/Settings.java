package com.aquaticstudios.aqualive.shared.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class Settings {
    private final YamlFile yaml;

    private String commandPermission;
    private String adminPermission;
    private String bypassCooldownPermission;
    private String serverName;
    private boolean preloadSkins;
    private List<String> aliases = new ArrayList<>();
    private Map<String, StreamPlatform> platforms = new LinkedHashMap<>();
    private Set<String> blacklistedServers = new HashSet<>();

    private Settings(final YamlFile yaml) {
        this.yaml = yaml;
    }

    public static Settings load(final Path dataFolder) throws IOException {
        final Settings settings = new Settings(YamlFile.load(dataFolder, "config.yml"));
        settings.parse();
        return settings;
    }

    public void reload() throws IOException {
        yaml.reload();
        parse();
    }

    private void parse() {
        commandPermission = yaml.getString("commands.permission", "aqualive.use");
        adminPermission = yaml.getString("commands.admin-permission", "aqualive.admin");
        bypassCooldownPermission = yaml.getString("permissions.bypass-cooldown", "aqualive.bypass");
        serverName = yaml.getString("server-name", "");
        preloadSkins = yaml.getBoolean("preload-skins", true);
        aliases = yaml.getStringList("commands.aliases");

        platforms = new LinkedHashMap<>();
        for (final String id : yaml.getKeys("platforms")) {
            final String domain = yaml.getString("platforms." + id + ".domain", null);
            if (domain == null) continue;
            platforms.put(id, new StreamPlatform(
                    id,
                    domain.toLowerCase(Locale.ROOT),
                    yaml.getString("platforms." + id + ".permission", null),
                    yaml.getInt("platforms." + id + ".cooldown", 0)));
        }

        blacklistedServers = new HashSet<>();
        for (final String server : yaml.getStringList("blacklist-server-send")) {
            blacklistedServers.add(server.toLowerCase(Locale.ROOT));
        }
    }

    public String commandPermission() {
        return commandPermission;
    }

    public String adminPermission() {
        return adminPermission;
    }

    public String bypassCooldownPermission() {
        return bypassCooldownPermission;
    }

    public String serverName() {
        return serverName;
    }

    public boolean preloadSkins() {
        return preloadSkins;
    }

    public List<String> aliases() {
        return aliases;
    }

    public Map<String, StreamPlatform> platforms() {
        return platforms;
    }

    public StreamPlatform platformForDomain(final String domain) {
        for (final StreamPlatform platform : platforms.values()) {
            if (domain.endsWith(platform.domain())) return platform;
        }
        return null;
    }

    public boolean isBlacklisted(final String server) {
        return server != null && blacklistedServers.contains(server.toLowerCase(Locale.ROOT));
    }

    public Set<String> blacklistedServers() {
        return blacklistedServers;
    }
}
