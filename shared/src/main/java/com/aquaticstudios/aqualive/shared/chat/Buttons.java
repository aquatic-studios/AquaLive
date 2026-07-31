package com.aquaticstudios.aqualive.shared.chat;

import com.aquaticstudios.aqualive.shared.config.YamlFile;
import com.aquaticstudios.aqualive.shared.placeholders.Placeholders;
import com.github.senkex.centermessage.CenterMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.List;

public final class Buttons {
    private static final String CLICK_URL = "url";

    private static final String CLICK_SERVER = "server";

    public static Component build(final YamlFile messages,
                                  final String buttonId,
                                  final Placeholders placeholders,
                                  final boolean serverClickAllowed) {
        final String base = "buttons." + buttonId;
        final String rawText = messages.getString(base + ".text", null);
        if (rawText == null) return Component.empty();

        Component button = CenterMessage.parse(placeholders.apply(rawText));

        final List<String> hover = messages.getStringList(base + ".hover");
        if (!hover.isEmpty()) {
            button = button.hoverEvent(HoverEvent.showText(
                    CenterMessage.parse(placeholders.apply(String.join("\n", hover)))));
        }

        final String type = messages.getString(base + ".click.type", null);
        final String rawValue = messages.getString(base + ".click.value", null);
        if (type == null || rawValue == null) return button;

        final String value = placeholders.apply(rawValue);
        if (value.isEmpty()) return button;

        if (CLICK_URL.equalsIgnoreCase(type)) {
            return button.clickEvent(ClickEvent.openUrl(value));
        }
        if (CLICK_SERVER.equalsIgnoreCase(type) && serverClickAllowed) {
            return button.clickEvent(ClickEvent.runCommand("/server " + value));
        }
        return button;
    }
}
