package com.aquaticstudios.aqualive.shared.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigAudit {
    private static final Pattern BUTTON_TOKEN = Pattern.compile("%button_([A-Za-z0-9_-]+)%");

    private final List<String> problems = new ArrayList<>();

    private ConfigAudit(final Settings settings, final YamlFile platforms, final YamlFile webhook) {
        check(settings, platforms, webhook);
    }

    public static ConfigAudit run(final Settings settings, final Platforms platforms, final Webhook webhook) {
        return new ConfigAudit(settings, platforms.yaml(), webhook.yaml());
    }

    public List<String> problems() {
        return problems;
    }

    public boolean isClean() {
        return problems.isEmpty();
    }

    private void check(final Settings settings, final YamlFile platforms, final YamlFile webhook) {
        final Set<String> loaded = settings.platforms().keySet();
        final Set<String> advertised = platforms.getKeys("advertisement");
        final Set<String> buttons = platforms.getKeys("buttons");
        final Set<String> hooked = webhook.getKeys("platforms");
        final boolean webhooksOn = webhook.getBoolean("enabled", false);

        for (final String id : settings.yaml().getKeys("platforms")) {
            if (!loaded.contains(id)) {
                problems.add("config.yml platform '" + id + "' has no 'domain' line, so it was skipped");
            }
        }

        for (final String id : loaded) {
            if (!advertised.contains(id)) {
                problems.add("platforms.yml has no 'advertisement." + id + "' block, '" + id
                        + "' would announce nothing");
                continue;
            }
            for (final String button : buttonTokens(platforms, "advertisement." + id)) {
                if (!buttons.contains(button)) {
                    problems.add("platforms.yml has no 'buttons." + button + "' block, %button_" + button
                            + "% used by '" + id + "' falls back to the link button");
                }
            }
        }

        if (webhooksOn) {
            for (final String id : loaded) {
                if (!hooked.contains(id)) {
                    problems.add("webhook.yml has no 'platforms." + id + "' block, no discord message is sent for '"
                            + id + "'");
                }
            }
        }

        for (final String id : advertised) {
            if (!loaded.contains(id)) {
                problems.add("platforms.yml advertises '" + id
                        + "' but config.yml has no matching platform, /live can never match it");
            }
        }

        for (final String id : hooked) {
            if (!loaded.contains(id)) {
                problems.add("webhook.yml has 'platforms." + id
                        + "' but config.yml has no matching platform, that webhook is never used");
            }
        }
    }

    private static Set<String> buttonTokens(final YamlFile yaml, final String path) {
        final Set<String> out = new LinkedHashSet<>();
        collect(yaml.get(path), out);
        return out;
    }

    private static void collect(final Object node, final Set<String> out) {
        if (node == null) return;

        if (node instanceof Map<?, ?> map) {
            for (final Object value : map.values()) collect(value, out);
            return;
        }
        if (node instanceof Iterable<?> list) {
            for (final Object value : list) collect(value, out);
            return;
        }

        final Matcher matcher = BUTTON_TOKEN.matcher(String.valueOf(node));
        while (matcher.find()) out.add(matcher.group(1));
    }
}
