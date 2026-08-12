package codes.antti.bluemap.placeholdernames;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.PlayerDisplayNameProvider;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class BlueMapPlaceholderNamesSpigot extends JavaPlugin {
    private final ConcurrentMap<UUID, String> displayNames = new ConcurrentHashMap<>();
    private final Set<UUID> failedPlayers = ConcurrentHashMap.newKeySet();
    private final Consumer<BlueMapAPI> blueMapEnableListener = this::onBlueMapEnable;
    private final Consumer<BlueMapAPI> blueMapDisableListener = api -> deactivate();

    private volatile String template;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getScheduler().runTaskTimer(this, this::refreshCache, 0L, 20L);

        BlueMapAPI.onEnable(blueMapEnableListener);
        BlueMapAPI.onDisable(blueMapDisableListener);
    }

    @Override
    public void onDisable() {
        BlueMapAPI.unregisterListener(blueMapEnableListener);
        BlueMapAPI.unregisterListener(blueMapDisableListener);
        deactivate();
    }

    private void onBlueMapEnable(BlueMapAPI api) {
        deactivate();
        reloadConfig();

        String configuredTemplate = getConfig().getString("template");
        if (configuredTemplate == null || configuredTemplate.isBlank()) {
            getLogger().severe("No display-name template is configured. Set 'template' in config.yml and reload BlueMap.");
            return;
        }

        template = configuredTemplate;
        PlayerDisplayNameProvider fallback = api.getPlugin().getPlayerDisplayNameProvider();
        api.getPlugin().setPlayerDisplayNameProvider(uuid -> {
            String displayName = displayNames.get(uuid);
            return displayName != null ? displayName : fallback.get(uuid);
        });
    }

    private void refreshCache() {
        String currentTemplate = template;
        if (currentTemplate == null) return;

        Set<UUID> onlinePlayers = new HashSet<>();
        for (var player : getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            onlinePlayers.add(uuid);

            try {
                String parsedName = PlaceholderAPI.setPlaceholders(player, currentTemplate);
                displayNames.put(uuid, ChatColor.stripColor(parsedName));
                failedPlayers.remove(uuid);
            } catch (RuntimeException exception) {
                if (failedPlayers.add(uuid)) {
                    getLogger().log(Level.WARNING, "Failed to resolve display-name placeholders for " + uuid, exception);
                }
            }
        }

        displayNames.keySet().retainAll(onlinePlayers);
        failedPlayers.retainAll(onlinePlayers);
    }

    private void deactivate() {
        template = null;
        displayNames.clear();
        failedPlayers.clear();
    }
}
