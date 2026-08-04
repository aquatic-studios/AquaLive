package com.aquaticstudios.aqualive.platforms.velocity;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

final class VelocityBridge {
    private static final String KYORI = String.join(".", "net", "kyori");
    private static final String ADVENTURE = KYORI + ".adventure";

    private static final GsonComponentSerializer JSON = GsonComponentSerializer.gson();

    private static final Class<?> AUDIENCE = type(ADVENTURE + ".audience.Audience");
    private static final Class<?> COMPONENT = type(ADVENTURE + ".text.Component");
    private static final Class<?> TITLE = type(ADVENTURE + ".title.Title");
    private static final Class<?> TIMES = type(ADVENTURE + ".title.Title$Times");
    private static final Class<?> SOUND = type(ADVENTURE + ".sound.Sound");
    private static final Class<?> SOURCE = type(ADVENTURE + ".sound.Sound$Source");
    private static final Class<?> KEY = type(ADVENTURE + ".key.Key");
    private static final Class<?> BOSS_BAR = type(ADVENTURE + ".bossbar.BossBar");
    private static final Class<?> BAR_COLOR = type(ADVENTURE + ".bossbar.BossBar$Color");
    private static final Class<?> BAR_OVERLAY = type(ADVENTURE + ".bossbar.BossBar$Overlay");

    private static final Class<?> GSON_SERIALIZER =
            type(ADVENTURE + ".text.serializer.gson.GsonComponentSerializer");
    private static final Object GSON = gson();
    private static final Method DESERIALIZE = deserializeMethod();

    private static final Map<BossBar, Object> BARS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    static void sendMessage(final Object target, final Component message) {
        call(target, "sendMessage", COMPONENT, component(message));
    }

    static void sendActionBar(final Object target, final Component message) {
        call(target, "sendActionBar", COMPONENT, component(message));
    }

    static void showTitle(final Object target, final Title title) {
        final Title.Times times = title.times();
        final Object converted = invoke(TITLE, "title",
                new Class<?>[]{COMPONENT, COMPONENT, TIMES},
                component(title.title()),
                component(title.subtitle()),
                invoke(TIMES, "times",
                        new Class<?>[]{Duration.class, Duration.class, Duration.class},
                        times == null ? Duration.ofMillis(500) : times.fadeIn(),
                        times == null ? Duration.ofMillis(3000) : times.stay(),
                        times == null ? Duration.ofMillis(500) : times.fadeOut()));

        call(target, "showTitle", TITLE, converted);
    }

    static void playSound(final Object target, final Sound sound) {
        final Object key = invoke(KEY, "key", new Class<?>[]{String.class}, sound.name().asString());
        final Object converted = invoke(SOUND, "sound",
                new Class<?>[]{KEY, SOURCE, float.class, float.class},
                key, constant(SOURCE, sound.source().name()), sound.volume(), sound.pitch());

        call(target, "playSound", SOUND, converted);
    }

    static void showBossBar(final Object target, final BossBar bar) {
        call(target, "showBossBar", BOSS_BAR, bossBar(bar));
    }

    static void hideBossBar(final Object target, final BossBar bar) {
        final Object converted = BARS.get(bar);
        if (converted == null) return;

        call(target, "hideBossBar", BOSS_BAR, converted);
    }

    private static Object bossBar(final BossBar bar) {
        synchronized (BARS) {
            Object converted = BARS.get(bar);
            if (converted != null) return converted;

            converted = invoke(BOSS_BAR, "bossBar",
                    new Class<?>[]{COMPONENT, float.class, BAR_COLOR, BAR_OVERLAY},
                    component(bar.name()),
                    bar.progress(),
                    constant(BAR_COLOR, bar.color().name()),
                    constant(BAR_OVERLAY, bar.overlay().name()));

            BARS.put(bar, converted);
            return converted;
        }
    }

    private static Object component(final Component message) {
        if (DESERIALIZE == null) throw new IllegalStateException("Velocity does not expose Adventure");
        try {
            return DESERIALIZE.invoke(GSON, JSON.serialize(message == null ? Component.empty() : message));
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Could not convert a component for Velocity", ex);
        }
    }

    private static void call(final Object target, final String name,
                             final Class<?> argument, final Object value) {
        try {
            AUDIENCE.getMethod(name, argument).invoke(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Velocity rejected " + name, ex);
        }
    }

    private static Object invoke(final Class<?> owner, final String name,
                                 final Class<?>[] signature, final Object... arguments) {
        try {
            return owner.getMethod(name, signature).invoke(null, arguments);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Could not build " + owner.getSimpleName(), ex);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object constant(final Class<?> type, final String name) {
        return Enum.valueOf((Class<Enum>) type, name);
    }

    private static Object gson() {
        try {
            return GSON_SERIALIZER.getMethod("gson").invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method deserializeMethod() {
        if (GSON_SERIALIZER == null) return null;

        for (final Method candidate : GSON_SERIALIZER.getMethods()) {
            if (!"deserialize".equals(candidate.getName())) continue;
            if (candidate.getParameterCount() != 1) continue;
            if (candidate.getParameterTypes()[0].isAssignableFrom(String.class)) return candidate;
        }
        return null;
    }

    private static Class<?> type(final String name) {
        try {
            return Class.forName(name);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
