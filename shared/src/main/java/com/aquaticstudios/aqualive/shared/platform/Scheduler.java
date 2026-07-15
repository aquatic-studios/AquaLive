package com.aquaticstudios.aqualive.shared.platform;

@FunctionalInterface
public interface Scheduler {
    void sync(Runnable task);
}
