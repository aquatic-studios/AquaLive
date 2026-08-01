package com.aquaticstudios.aqualive.shared.features;

import com.aquaticstudios.aqualive.shared.config.StreamPlatform;
import com.aquaticstudios.aqualive.shared.config.Webhook;
import com.aquaticstudios.aqualive.shared.config.YamlFile;
import com.aquaticstudios.aqualive.shared.placeholders.Placeholders;
import com.aquaticstudios.aqualive.shared.platform.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WebhookSender {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final Platform platform;
    private final Webhook webhook;
    private final Set<String> warned = ConcurrentHashMap.newKeySet();

    public WebhookSender(final Platform platform, final Webhook webhook) {
        this.platform = platform;
        this.webhook = webhook;
    }

    public void resetWarnings() {
        warned.clear();
    }

    public void send(final StreamPlatform stream, final Placeholders placeholders) {
        final YamlFile yaml = webhook.yaml();
        if (!yaml.getBoolean("enabled", false)) return;

        final String id = stream.id();
        final String section = "platforms." + id;
        if (!yaml.contains(section) || !yaml.getBoolean(section + ".enabled", true)) return;

        final String url = resolve(yaml, section, "url", placeholders);
        if (!url.startsWith("http://") && !url.startsWith("https://")) return;

        placeholders.set("mention", resolve(yaml, section, "mention", placeholders));
        post(url, buildPayload(yaml, section, placeholders), id);
    }

    private String buildPayload(final YamlFile yaml, final String section, final Placeholders p) {
        final StringBuilder json = new StringBuilder("{");
        boolean first = true;

        final String content = resolve(yaml, section, "content", p);
        if (!content.isEmpty()) first = field(json, first, "content", quote(content));

        final String username = resolve(yaml, section, "username", p);
        if (!username.isEmpty()) first = field(json, first, "username", quote(username));

        final String avatar = resolve(yaml, section, "avatar-url", p);
        if (!avatar.isEmpty()) first = field(json, first, "avatar_url", quote(avatar));

        field(json, first, "embeds", "[" + buildEmbed(yaml, section, p) + "]");
        return json.append('}').toString();
    }

    private String buildEmbed(final YamlFile yaml, final String section, final Placeholders p) {
        final String base = section + ".embed";
        final StringBuilder embed = new StringBuilder("{");
        boolean first = true;

        final int color = parseColor(yaml.getString(section + ".color", ""));
        if (color >= 0) first = field(embed, first, "color", Integer.toString(color));

        final String title = p.apply(yaml.getString(base + ".title", ""));
        if (!title.isEmpty()) first = field(embed, first, "title", quote(title));

        final String url = p.apply(yaml.getString(base + ".url", ""));
        if (!url.isEmpty()) first = field(embed, first, "url", quote(url));

        final String description = p.apply(yaml.getString(base + ".description", ""));
        if (!description.isEmpty()) first = field(embed, first, "description", quote(description));

        final String author = buildAuthor(yaml, base, p);
        if (author != null) first = field(embed, first, "author", author);

        final String thumbnail = p.apply(yaml.getString(base + ".thumbnail", ""));
        if (!thumbnail.isEmpty()) first = field(embed, first, "thumbnail", "{\"url\":" + quote(thumbnail) + "}");

        final String image = p.apply(yaml.getString(base + ".image", ""));
        if (!image.isEmpty()) first = field(embed, first, "image", "{\"url\":" + quote(image) + "}");

        final String fields = buildFields(yaml, base, p);
        if (fields != null) first = field(embed, first, "fields", fields);

        final String footer = buildFooter(yaml, base, p);
        if (footer != null) first = field(embed, first, "footer", footer);

        if (yaml.getBoolean(base + ".timestamp", false)) {
            first = field(embed, first, "timestamp", quote(Instant.now().toString()));
        }

        return embed.append('}').toString();
    }

    private String buildAuthor(final YamlFile yaml, final String base, final Placeholders p) {
        final String name = p.apply(yaml.getString(base + ".author.name", ""));
        if (name.isEmpty()) return null;

        final StringBuilder author = new StringBuilder("{");
        boolean first = field(author, true, "name", quote(name));

        final String icon = p.apply(yaml.getString(base + ".author.icon-url", ""));
        if (!icon.isEmpty()) first = field(author, first, "icon_url", quote(icon));

        final String url = p.apply(yaml.getString(base + ".author.url", ""));
        if (!url.isEmpty()) field(author, first, "url", quote(url));

        return author.append('}').toString();
    }

    private String buildFooter(final YamlFile yaml, final String base, final Placeholders p) {
        final String text = p.apply(yaml.getString(base + ".footer.text", ""));
        if (text.isEmpty()) return null;

        final StringBuilder footer = new StringBuilder("{");
        final boolean first = field(footer, true, "text", quote(text));

        final String icon = p.apply(yaml.getString(base + ".footer.icon-url", ""));
        if (!icon.isEmpty()) field(footer, first, "icon_url", quote(icon));

        return footer.append('}').toString();
    }

    @SuppressWarnings("unchecked")
    private String buildFields(final YamlFile yaml, final String base, final Placeholders p) {
        final Object raw = yaml.get(base + ".fields");
        if (!(raw instanceof List<?> list) || list.isEmpty()) return null;

        final StringBuilder fields = new StringBuilder("[");
        boolean first = true;
        for (final Object item : list) {
            if (!(item instanceof Map)) continue;
            final Map<String, Object> field = (Map<String, Object>) item;

            final String name = p.apply(text(field.get("name")));
            final String value = p.apply(text(field.get("value")));
            if (name.isEmpty() && value.isEmpty()) continue;

            final boolean inline = Boolean.parseBoolean(text(field.get("inline")));
            if (!first) fields.append(',');
            fields.append("{\"name\":").append(quote(name))
                    .append(",\"value\":").append(quote(value))
                    .append(",\"inline\":").append(inline).append('}');
            first = false;
        }
        return first ? null : fields.append(']').toString();
    }

    private void post(final String url, final String json, final String id) {
        final HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "AquaLive")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
        } catch (IllegalArgumentException ex) {
            warnOnce(id + "|config", "webhook.yml url for '" + id + "' is malformed, skipping: " + url);
            return;
        }

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> {
                    if (error != null) {
                        warnOnce(id + "|delivery",
                                "Webhook POST failed for '" + id + "', check the url: " + error.getMessage());
                        return;
                    }
                    final int code = response.statusCode();
                    if (code < 200 || code >= 300) {
                        warnOnce(id + "|delivery",
                                "Webhook for '" + id + "' returned HTTP " + code + ", check the url");
                    }
                });
    }

    private void warnOnce(final String key, final String message) {
        if (warned.add(key)) platform.logger().warn(message);
    }

    private String resolve(final YamlFile yaml, final String section, final String key, final Placeholders p) {
        String value = yaml.getString(section + "." + key, null);
        if (value == null) value = yaml.getString("defaults." + key, "");
        return p.apply(value);
    }

    private static int parseColor(final String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return -1;
        if (value.startsWith("#")) value = value.substring(1);
        try {
            return value.length() == 6 ? Integer.parseInt(value, 16) : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static boolean field(final StringBuilder sb, final boolean first, final String key, final String value) {
        if (!first) sb.append(',');
        sb.append('"').append(key).append("\":").append(value);
        return false;
    }

    private static String text(final Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String quote(final String value) {
        final StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }
}
