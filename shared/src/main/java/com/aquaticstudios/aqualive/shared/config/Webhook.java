package com.aquaticstudios.aqualive.shared.config;

import java.io.IOException;
import java.nio.file.Path;

public final class Webhook {
    private final YamlFile yaml;

    private Webhook(final YamlFile yaml) {
        this.yaml = yaml;
    }

    public static Webhook load(final Path dataFolder) throws IOException {
        return new Webhook(YamlFile.load(dataFolder, "webhook.yml"));
    }

    public void reload() throws IOException {
        yaml.reload();
    }

    public YamlFile yaml() {
        return yaml;
    }
}
