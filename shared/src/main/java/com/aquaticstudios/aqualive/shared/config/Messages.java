package com.aquaticstudios.aqualive.shared.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Messages {
    private static final String PREFIX_TOKEN = "%prefix%";

    private final YamlFile yaml;

    private Messages(final YamlFile yaml) {
        this.yaml = yaml;
    }

    public static Messages load(final Path dataFolder) throws IOException {
        return new Messages(YamlFile.load(dataFolder, "messages.yml"));
    }

    public void reload() throws IOException {
        yaml.reload();
    }

    public YamlFile yaml() {
        return yaml;
    }

    public String prefix() {
        return yaml.getString("prefix", "");
    }

    public String get(final String path) {
        return applyPrefix(yaml.getString("messages." + path, ""));
    }

    public List<String> list(final String path) {
        final List<String> raw = yaml.getStringList(path);
        final List<String> out = new ArrayList<>(raw.size());
        for (final String line : raw) {
            out.add(applyPrefix(line));
        }
        return out;
    }

    private String applyPrefix(final String line) {
        return line.contains(PREFIX_TOKEN) ? line.replace(PREFIX_TOKEN, prefix()) : line;
    }
}
