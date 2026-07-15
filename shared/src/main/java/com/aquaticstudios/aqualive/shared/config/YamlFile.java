package com.aquaticstudios.aqualive.shared.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class YamlFile {
    private final Path file;
    private final String resource;

    private Map<String, Object> root = new LinkedHashMap<>();

    private YamlFile(final Path file, final String resource) {
        this.file = file;
        this.resource = resource;
    }

    public static YamlFile load(final Path dataFolder, final String name) throws IOException {
        final YamlFile yaml = new YamlFile(dataFolder.resolve(name), name);
        yaml.saveDefaultIfAbsent(dataFolder);
        yaml.reload();
        return yaml;
    }

    private void saveDefaultIfAbsent(final Path dataFolder) throws IOException {
        if (Files.exists(file)) return;
        Files.createDirectories(dataFolder);
        try (InputStream in = YamlFile.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) throw new IOException("Bundled resource missing from the jar: " + resource);
            Files.copy(in, file);
        }
    }

    @SuppressWarnings("unchecked")
    public void reload() throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            final Object loaded = new Yaml().load(reader);
            root = (loaded instanceof Map) ? (Map<String, Object>) loaded : new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    public Object get(final String path) {
        Object current = root;
        for (final String part : path.split("\\.")) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(part);
            if (current == null) return null;
        }
        return current;
    }

    public String getString(final String path, final String def) {
        final Object v = get(path);
        return v == null ? def : String.valueOf(v);
    }

    public int getInt(final String path, final int def) {
        final Object v = get(path);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v == null) return def;
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }

    public boolean getBoolean(final String path, final boolean def) {
        final Object v = get(path);
        if (v instanceof Boolean) return (Boolean) v;
        return v == null ? def : Boolean.parseBoolean(String.valueOf(v));
    }

    public List<String> getStringList(final String path) {
        final Object v = get(path);
        if (!(v instanceof List)) return Collections.emptyList();
        final List<String> out = new ArrayList<>();
        for (final Object o : (List<?>) v) {
            if (o != null) out.add(String.valueOf(o));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSection(final String path) {
        final Object v = get(path);
        return (v instanceof Map) ? (Map<String, Object>) v : null;
    }

    public Set<String> getKeys(final String path) {
        final Map<String, Object> section = getSection(path);
        return section == null ? Collections.emptySet() : section.keySet();
    }

    public boolean contains(final String path) {
        return get(path) != null;
    }
}
