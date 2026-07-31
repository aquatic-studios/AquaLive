package com.aquaticstudios.aqualive.shared.placeholders;

import com.aquaticstudios.aqualive.shared.hook.PlaceholderHook;
import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Placeholders {
    private final Map<String, String> values = new LinkedHashMap<>();
    private PlaceholderHook resolver = PlaceholderHook.NONE;
    private AquaPlayer target;

    public static Placeholders create() {
        return new Placeholders();
    }

    public Placeholders set(final String key, final String value) {
        values.put(key, value == null ? "" : value);
        return this;
    }

    public Placeholders resolver(final PlaceholderHook resolver, final AquaPlayer target) {
        this.resolver = resolver == null ? PlaceholderHook.NONE : resolver;
        this.target = target;
        return this;
    }

    public String apply(final String text) {
        if (text == null || text.isEmpty()) return "";
        String out = text;
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            out = out.replace('%' + entry.getKey() + '%', entry.getValue());
        }
        return resolver.resolve(target, out);
    }
}
