package codes.antti.bluemap.displaynames;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.PlayerDisplayNameProvider;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class BlueMapDisplayNames implements DedicatedServerModInitializer {
    private volatile MinecraftServer server;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.server = server);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.server = null);

        BlueMapAPI.onEnable(api -> {
            PlayerDisplayNameProvider fallback = api.getPlugin().getPlayerDisplayNameProvider();

            api.getPlugin().setPlayerDisplayNameProvider((uuid) -> {
                if (this.server == null) return fallback.get(uuid);
                var player = this.server.getPlayerList().getPlayer(uuid);
                if (player == null) return fallback.get(uuid);
                return player.getDisplayName().getString();
            });
        });
    }
}
