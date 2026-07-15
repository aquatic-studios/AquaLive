package com.aquaticstudios.aqualive.shared.chat;

import com.aquaticstudios.aqualive.shared.placeholders.Placeholders;
import com.github.senkex.centermessage.CenterMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatRenderer {
    public static final LegacyComponentSerializer SECTION_HEX = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final Pattern CENTER_TAG = Pattern.compile("(?is)^\\s*<center>(.*?)</center>\\s*$");

    private ChatRenderer() {
    }

    public static Component line(final String raw,
                                 final Placeholders placeholders,
                                 final Map<String, Component> buttons) {
        if (raw == null) return Component.empty();

        String text = placeholders.apply(raw);

        boolean centered = false;
        final Matcher matcher = CENTER_TAG.matcher(text);
        if (matcher.matches()) {
            centered = true;
            text = matcher.group(1);
        }

        Component component = splice(CenterMessage.parse(text), buttons);
        return centered ? CenterMessage.centerComponent(component) : component;
    }

    public static Component headRow(final String rendered, final Map<String, Component> buttons) {
        if (rendered == null || rendered.isEmpty()) return Component.empty();
        return splice(SECTION_HEX.deserialize(rendered), buttons);
    }

    public static Component text(final String raw, final Placeholders placeholders) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        return CenterMessage.parse(placeholders.apply(raw));
    }

    private static Component splice(final Component component, final Map<String, Component> buttons) {
        if (buttons == null || buttons.isEmpty()) return component;
        Component out = component;
        for (final Map.Entry<String, Component> entry : buttons.entrySet()) {
            out = out.replaceText(TextReplacementConfig.builder()
                    .matchLiteral(entry.getKey())
                    .replacement(entry.getValue())
                    .build());
        }
        return out;
    }
}
