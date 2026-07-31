package com.aquaticstudios.aqualive.shared.config;

import java.io.IOException;
import java.nio.file.Path;

public final class Platforms {
    private final YamlFile yaml;

    private Platforms(final YamlFile yaml) {
        this.yaml = yaml;
    }

    public static Platforms load(final Path dataFolder) throws IOException {
        return new Platforms(YamlFile.load(dataFolder, "platforms.yml"));
    }

    public void reload() throws IOException {
        yaml.reload();
    }

    public YamlFile yaml() {
        return yaml;
    }
}
