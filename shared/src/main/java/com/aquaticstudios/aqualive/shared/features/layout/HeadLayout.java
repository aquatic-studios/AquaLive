package com.aquaticstudios.aqualive.shared.features.layout;

import com.aquaticstudios.aqualive.shared.chat.ChatRenderer;
import com.aquaticstudios.aqualive.shared.config.YamlFile;
import com.aquaticstudios.aqualive.shared.placeholders.Placeholders;
import com.github.senkex.centermessage.CenterMessage;
import com.github.senkex.headrender.HeadRender;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class HeadLayout {
    private static final String MARKER = "<hd>";

    private static final int HEAD_ROWS = 8;

    public static String validate(final YamlFile messages, final String section) {
        final List<String> rows = messages.getStringList(section + ".broadcast.chat.head.rows");
        if (rows.isEmpty()) {
            return section + ".broadcast.chat.head.rows is missing or empty";
        }
        if (rows.size() != HEAD_ROWS) {
            return section + ".broadcast.chat.head.rows has " + rows.size()
                    + " rows, but a head is exactly " + HEAD_ROWS + " rows tall";
        }
        return null;
    }

    public static CompletableFuture<List<Component>> build(final YamlFile messages,
                                                           final String section,
                                                           final Placeholders placeholders,
                                                           final Map<String, Component> buttons) {
        final String skin = placeholders.apply(
                messages.getString(section + ".broadcast.chat.head.skin", "%player%"));

        final String fallbackSkin = placeholders.apply(
                messages.getString(section + ".broadcast.chat.head.fallback-skin", ""));

        final List<String> rows = new ArrayList<>();
        for (final String raw : messages.getStringList(section + ".broadcast.chat.head.rows")) {
            rows.add(prepare(raw, placeholders));
        }

        return HeadRender.renderRows(skin, rows)
                .exceptionallyCompose(error -> {
                    if (fallbackSkin.isEmpty() || fallbackSkin.equalsIgnoreCase(skin)) {
                        return CompletableFuture.failedFuture(error);
                    }
                    return HeadRender.renderRows(fallbackSkin, rows);
                })
                .thenApply(rendered -> {
                    final List<Component> out = new ArrayList<>(rendered.size());
                    for (final String line : rendered) {
                        out.add(ChatRenderer.headRow(line, buttons));
                    }
                    return out;
                });
    }

    private static String prepare(final String raw, final Placeholders placeholders) {
        if (raw == null || raw.isEmpty()) return MARKER;

        final boolean marked = raw.startsWith(MARKER);
        final String body = marked ? raw.substring(MARKER.length()) : raw;
        final String coloured = CenterMessage.colorize(placeholders.apply(body));

        return marked ? MARKER + coloured : coloured;
    }
}