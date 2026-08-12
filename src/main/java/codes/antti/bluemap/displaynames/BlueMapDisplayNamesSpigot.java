package codes.antti.bluemap.displaynames;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.PlayerDisplayNameProvider;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlueMapDisplayNamesSpigot extends JavaPlugin {
    @Override
    public void onEnable() {
        BlueMapAPI.onEnable(api -> {
            PlayerDisplayNameProvider fallback = api.getPlugin().getPlayerDisplayNameProvider();

            api.getPlugin().setPlayerDisplayNameProvider(uuid -> {
                var player = getServer().getPlayer(uuid);
                if (player == null) return fallback.get(uuid);
                return ChatColor.stripColor(player.getDisplayName());
            });
        });
    }
}
