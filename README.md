# BlueMap-PlaceholderNames

BlueMap-PlaceholderNames replaces BlueMap's live player names with a placeholder template. Placeholder values are refreshed on the Minecraft server thread every second and cached for BlueMap's web requests.

## Requirements

- BlueMap
- Spigot/Paper: [PlaceholderAPI](https://placeholderapi.com/) 2.12.3 or newer
- Fabric: Fabric API and [Text Placeholder API](https://placeholders.pb4.eu/) 3.1.0-beta.1 or newer

PlaceholderAPI expansions are installed separately. For example, `%player_displayname%` requires the Player expansion.

## Configuration

Start the server once to generate the platform's configuration file. Set a non-empty `template`, then restart the server or reload BlueMap. The addon does not define its own placeholders; use the syntax and placeholders provided by the platform API.

Spigot/Paper uses `plugins/BlueMap-PlaceholderNames/config.yml`:

```yaml
template: "%player_displayname%"
```

Fabric uses `config/bluemap-placeholdernames.properties`:

```properties
template=%player:displayname%
```

If the template is missing or blank, the addon logs an error and leaves BlueMap's existing player-name provider unchanged. Configuration is reloaded when BlueMap is reloaded.

Names are plain text in BlueMap. Legacy Bukkit color formatting is removed on Spigot/Paper, and Minecraft text-component formatting is flattened on Fabric.

## Building

```shell
./gradlew build
```
