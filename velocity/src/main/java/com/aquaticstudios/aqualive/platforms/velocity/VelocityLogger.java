package com.aquaticstudios.aqualive.platforms.velocity;

import com.aquaticstudios.aqualive.shared.platform.PluginLogger;
import org.slf4j.Logger;

public final class VelocityLogger implements PluginLogger {
    private final Logger logger;

    public VelocityLogger(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(final String message) {
        logger.info(message);
    }

    @Override
    public void warn(final String message) {
        logger.warn(message);
    }

    @Override
    public void error(final String message, final Throwable error) {
        logger.error(message, error);
    }
}
