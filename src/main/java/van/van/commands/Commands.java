package van.van.commands;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import van.van.Utils;
import van.van.VariablesStorage;

public class Commands {
    public PaperCommandManager<Player> registerCommands(PaperCommandManager<Player> manager) {
        // 예시:
        //   플레이타임: 32시간 27분
        manager.command(manager.commandBuilder("playtime")
                .argument(OfflinePlayerArgument.optional("player")).handler(ctx -> {
                    OfflinePlayer p = ctx.getOrDefault("player", ctx.getSender());
                    ctx.getSender().sendMessage(
                            Component.text("플레이타임: ").append(
                                    Component.text(DurationFormatUtils.formatDuration(
                                            Utils.getPlaytime(p) * 1000, "H:mm", true))
                                            .color(NamedTextColor.GREEN)
                            ));
                }));

        // 예시:
        //   스폰 위치: 132, 5, -94
        manager.command(manager.commandBuilder("spawnpoint").handler(ctx -> {
            Location loc = ctx.getSender().getRespawnLocation() == null ? new Location(Bukkit.getWorld("world"), 0, 64, 0) : ctx.getSender().getRespawnLocation().toBlockLocation();
            ctx.getSender().sendMessage(Component.text("스폰 위치: ")
                    .append(Component.text(loc.x()+", "+loc.y()+", "+loc.z()).color(NamedTextColor.GREEN)));
        }));

        manager.command(manager.commandBuilder("global").handler(ctx -> {
            VariablesStorage.localChat.put(ctx.getSender().getUniqueId(), false);
        }));
        manager.command(manager.commandBuilder("global").argument(StringArgument.greedy("message")).handler(ctx -> {
            Utils.sendGlobalChat(ctx.getSender(), "<"+ctx.getSender().getDisplayName()+"> "+ctx.get("message"));
        }));

        manager.command(manager.commandBuilder("global").handler(ctx -> {
            VariablesStorage.localChat.put(ctx.getSender().getUniqueId(), false);
        }));
        manager.command(manager.commandBuilder("global").argument(StringArgument.greedy("message")).handler(ctx -> {
            Utils.sendGlobalChat(ctx.getSender(), ctx.get("message"));
        }));

        return manager;
    }

}
