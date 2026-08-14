package com.aquaticstudios.aqualive.shared.features.layout;

import com.aquaticstudios.aqualive.shared.chat.ChatRenderer;
import com.aquaticstudios.aqualive.shared.config.YamlFile;
import com.aquaticstudios.aqualive.shared.placeholders.Placeholders;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ClassicLayout {
    public static List<Component> build(final YamlFile messages,
                                        final String section,
                                        final Placeholders placeholders,
                                        final Map<String, Component> buttons) {
        final List<Component> out = new ArrayList<>();
        for (final String raw : messages.getStringList(section + ".broadcast.chat.lines")) {
            out.add(ChatRenderer.line(raw, placeholders, buttons));
        }
        return out;
    }
}
