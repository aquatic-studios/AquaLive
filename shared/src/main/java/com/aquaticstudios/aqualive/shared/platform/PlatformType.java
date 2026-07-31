package com.aquaticstudios.aqualive.shared.platform;

public enum PlatformType {
    FOLIA("Folia", false),
    PURPUR("Purpur", false),
    PAPER("Paper", false),
    SPIGOT("Spigot", false),
    VELOCITY("Velocity", true),
    BUNGEE("BungeeCord", true);

    private final String displayName;
    private final boolean proxy;

    PlatformType(final String displayName, final boolean proxy) {
        this.displayName = displayName;
        this.proxy = proxy;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isProxy() {
        return proxy;
    }
}