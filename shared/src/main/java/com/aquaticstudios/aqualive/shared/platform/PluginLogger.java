package com.aquaticstudios.aqualive.shared.platform;

public interface PluginLogger {
    void info(String message);

    void warn(String message);

    void error(String message, Throwable error);
}