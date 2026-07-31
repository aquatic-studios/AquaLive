package com.aquaticstudios.aqualive.shared.platform;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class JavaLogger implements PluginLogger {
    private final Logger logger;

    public JavaLogger(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(final String message) {
        logger.info(message);
    }

    @Override
    public void warn(final String message) {
        logger.warning(message);
    }

    @Override
    public void error(final String message, final Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }
}