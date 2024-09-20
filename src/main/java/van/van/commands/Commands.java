package van.van.commands;

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.permission.CommandPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.util.permissions.CommandPermissions;
import van.van.*;

import java.time.Instant;

public class Commands {
    public PaperCommandManager<Player> registerCommands(PaperCommandManager<Player> manager) {
        // 예시:
        //   플레이타임: 32시간 27분
        manager.command(manager.commandBuilder("playtime", "플레이타임")
                .argument(OfflinePlayerArgument.optional("player")).handler(ctx -> {
                    OfflinePlayer p = ctx.getOrDefault("player", ctx.getSender());
                    ctx.getSender().sendMessage(
                            Component.text("플레이타임: ").append(
                                    Component.text(DurationFormatUtils.formatDuration(
                                            Utils.getPlaytime(p).toMillis(), "H:mm", true))
                                            .color(NamedTextColor.GREEN)
                            ));
                }));

        // 예시:
        //   스폰 위치: 132, 5, -94
        manager.command(manager.commandBuilder("spawnpoint", "home", "스폰포인트").handler(ctx -> {
            Location loc = ctx.getSender().getRespawnLocation() == null ? new Location(Bukkit.getWorld("world"), 0, 64, 0) : ctx.getSender().getRespawnLocation().toBlockLocation();
            ctx.getSender().sendMessage(Component.text("스폰 위치: ")
                    .append(Component.text(loc.x()+", "+loc.y()+", "+loc.z()).color(NamedTextColor.GREEN)));
        }));

        manager.command(manager.commandBuilder("global", "g", "전체채팅").handler(ctx -> {
            VariablesStorage.localChat.put(ctx.getSender().getUniqueId(), false);
        }));
        manager.command(manager.commandBuilder("global", "g", "전체채팅").argument(StringArgument.greedy("message")).handler(ctx -> {
            Utils.sendGlobalChat(ctx.getSender(), "<"+ctx.getSender().getDisplayName()+"> "+ctx.get("message"));
        }));

        manager.command(manager.commandBuilder("local", "l", "지역채팅").handler(ctx -> {
            VariablesStorage.localChat.put(ctx.getSender().getUniqueId(), true);
        }));
        manager.command(manager.commandBuilder("local", "l", "지역채팅").argument(StringArgument.greedy("message")).handler(ctx -> {
            Utils.sendLocalChat(ctx.getSender(), "<"+ctx.getSender().getDisplayName()+"> "+ctx.get("message"));
        }));
        // 예시:
        //   이전 위치: 534, 52, -577
        manager.command(manager.commandBuilder("previouslocation", "back", "이전위치").handler(ctx -> {
            ctx.getSender().sendMessage(Component.text("이전 위치: ")
                    .append(Component.text(
                            Utils.locationToString(VariablesStorage.backLocations.get(ctx.getSender().getUniqueId())))
                            .color(NamedTextColor.GREEN)));
        }));

        manager.command(manager.commandBuilder("whyamidead", "deathlocation", "사망위치").handler(ctx -> {
            ctx.getSender().sendMessage(Component.text(
                            Utils.locationToString(VariablesStorage.deathLocations.get(ctx.getSender().getUniqueId())))
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(" 에서 사망하셨습니다").color(NamedTextColor.WHITE)));
            ctx.getSender().sendMessage(VariablesStorage.deathReasons.get(ctx.getSender().getUniqueId())
                    .decoration(TextDecoration.ITALIC, true)
                    .color(NamedTextColor.GRAY));
        }));

        manager.command(manager.commandBuilder("ptset").permission("van.ptset")
                .argument(IntegerArgument.of("amount")).handler(ctx -> {
                    ctx.getSender().setStatistic(Statistic.PLAY_ONE_MINUTE, ctx.get("amount"));
                }));

        manager.command(manager.commandBuilder("invite").argument(PlayerArgument.of("player")).handler(ctx -> {
            if (((Player) ctx.get("player")).getUniqueId() == ctx.getSender().getUniqueId()) {
                ctx.getSender().sendMessage(Component.text("자기 자신에게 초대 요청을 보낼 수 없어요!").color(NamedTextColor.RED));
                return;
            }
            InviteManager.inviteRequests.add(new PlayerPair(ctx.getSender(), ctx.get("player")));
        }));

        //TODO: claim, invite/tpahere, slimechunk
        manager.command(manager.commandBuilder("w")
                .argument(PlayerArgument.of("player")).argument(StringArgument.greedy("message"))
                .handler(ctx -> {
                    ((Player) ctx.get("player")).sendMessage(VAN.mm.deserialize(
                            "<dark_gray>[<gray>%s</gray>-><gray>me</gray>]<white> %s"
                                    .formatted(((Player) ctx.get("player")).getName(), ctx.get("messge"))
                    ));
                    ctx.getSender().sendMessage(VAN.mm.deserialize(
                            "<dark_gray>[me-><gray>%s</gray>]<white> %s"
                                    .formatted(((Player) ctx.get("player")).getName(), ctx.get("messge"))
                    ));
                }));
        return manager;
    }



}
