package van.van.commands;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.*;
import van.van.Utils;

public class Commands {
    public void registerCommands() {
        // 예시:
        //   플레이타임: 32시간 27분
        new CommandAPICommand
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

        manager.command(manager.commandBuilder("g").handler(ctx -> {
            ctx.getSender()
        }));

        return manager;
    }

}
