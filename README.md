# Glacier

Glacier is a velocity plugin I wrote to get familiar with velocity plugin development. I wrote this plugin as a utility plugin for a Minecraft server that will handle timed announcements, commands for announcing messages through titles and subtitles, and action bars. I won't be frequently updating the code, and the code may contain bugs or exploits. Use this at your own risk.

## Commands and permissions

- `/glacier reload` | **glacier.admin** -  Reloads the config (announcements/main config/messages)
- `/glacier version` | **glacier.admin** -  Prints the plugin version
- `/gtitle` | **glacier.admin.title** -  Broadcast messages through titles/subtitles
- `/gactionbar` | **glacier.admin.actionbar** -  Broadcast messages through actionbar
- `/gannouncer` | **glacier.admin.announcer**
  - `/gannouncer reload` - Reload announcements
  - `/gannouncer list` - Prints loaded announcements
  - `/gannouncer view` - View loaded announcements
  - `/gannouncer send` - Manually trigger an announcement

Aliases for `glacier` command are `glc` and `glcr`
Downloads can be found in the release tab.

## Other Features

 - All messages are configurable through `messages.yml` file
 - Announcements and messages supports MiniMessage. You can use [Adventure Web UI](https://webui.advntr.dev/) to create customized messages with colors and click/chat events.
 - Added support [PAPIProxyBridge](https://github.com/WiIIiam278/PAPIProxyBridge). By default, disabled in config.
