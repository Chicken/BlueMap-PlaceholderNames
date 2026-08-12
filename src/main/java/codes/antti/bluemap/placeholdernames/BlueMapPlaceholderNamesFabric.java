package codes.antti.bluemap.placeholdernames;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.PlayerDisplayNameProvider;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BlueMapPlaceholderNamesFabric implements DedicatedServerModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("bluemap-placeholdernames");
    private static final String CONFIG_FILE = "bluemap-placeholdernames.properties";

    private final ConcurrentMap<UUID, String> displayNames = new ConcurrentHashMap<>();
    private final Set<UUID> failedPlayers = ConcurrentHashMap.newKeySet();
    private final NodeParser placeholderParser = NodeParser.builder().serverPlaceholders().build();

    private volatile String template;
    private int ticks;

    @Override
    public void onInitializeServer() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++ticks < 20) return;
            ticks = 0;
            refreshCache(server);
        });

        BlueMapAPI.onEnable(this::onBlueMapEnable);
        BlueMapAPI.onDisable(api -> deactivate());
    }

    private void onBlueMapEnable(BlueMapAPI api) {
        deactivate();

        String configuredTemplate = loadTemplate();
        if (configuredTemplate == null) return;

        template = configuredTemplate;
        PlayerDisplayNameProvider fallback = api.getPlugin().getPlayerDisplayNameProvider();
        api.getPlugin().setPlayerDisplayNameProvider(uuid -> {
            String displayName = displayNames.get(uuid);
            return displayName != null ? displayName : fallback.get(uuid);
        });
    }

    private void refreshCache(MinecraftServer server) {
        String currentTemplate = template;
        if (currentTemplate == null) return;

        Set<UUID> onlinePlayers = new HashSet<>();
        for (var player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            onlinePlayers.add(uuid);

            try {
                var context = ServerPlaceholderContext.of(player).asParserContext();
                String parsedName = placeholderParser.parseComponent(currentTemplate, context).getString();
                displayNames.put(uuid, parsedName);
                failedPlayers.remove(uuid);
            } catch (RuntimeException exception) {
                if (failedPlayers.add(uuid)) {
                    LOGGER.warn("Failed to resolve display-name placeholders for {}", uuid, exception);
                }
            }
        }

        displayNames.keySet().retainAll(onlinePlayers);
        failedPlayers.retainAll(onlinePlayers);
    }

    private String loadTemplate() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);

        try {
            if (Files.notExists(configPath)) {
                Files.createDirectories(configPath.getParent());
                InputStream defaultConfig = getClass().getResourceAsStream("/" + CONFIG_FILE);
                if (defaultConfig == null) throw new IOException("Missing default configuration resource: " + CONFIG_FILE);
                try (defaultConfig) {
                    Files.copy(defaultConfig, configPath);
                }
            }

            Properties properties = new Properties();
            try (var reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }

            String configuredTemplate = properties.getProperty("template");
            if (configuredTemplate == null || configuredTemplate.isBlank()) {
                LOGGER.error("No display-name template is configured. Set 'template' in {} and reload BlueMap.", configPath);
                return null;
            }

            return configuredTemplate;
        } catch (IOException exception) {
            LOGGER.error("Failed to load display-name configuration from {}", configPath, exception);
            return null;
        }
    }

    private void deactivate() {
        template = null;
        displayNames.clear();
        failedPlayers.clear();
    }
}
