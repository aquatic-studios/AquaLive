package com.aquaticstudios.aqualive.shared.platform;

import java.time.Duration;

public interface Scheduler {
    void sync(Runnable task);

    void later(Runnable task, Duration delay);
}
