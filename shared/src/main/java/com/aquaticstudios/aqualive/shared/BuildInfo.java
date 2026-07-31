package com.aquaticstudios.aqualive.shared;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class BuildInfo {
    public static final String VERSION = load();

    private static String load() {
        try (InputStream in = BuildInfo.class.getClassLoader().getResourceAsStream("aqualive.properties")) {
            if (in == null) return "unknown";
            final Properties props = new Properties();
            props.load(in);
            return props.getProperty("version", "unknown");
        } catch (IOException ex) {
            return "unknown";
        }
    }
}
