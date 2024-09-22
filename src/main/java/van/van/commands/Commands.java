package van.van.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import van.van.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Commands {
    public static LegacyPaperCommandManager<CommandSender> registerCommands(LegacyPaperCommandManager<CommandSender> manager) {
        // 예시:
        //   플레이타임: 32시간 27분
        manager.command(manager.commandBuilder("playtime", "플레이타임")
                .senderType(Player.class)
                .optional("player", OfflinePlayerParser.offlinePlayerParser()).handler(ctx -> {
                    OfflinePlayer p = ctx.getOrDefault("player", ctx.sender());
                    if (!p.hasPlayedBefore()) {
                        ctx.sender().sendMessage(VAN.mm.deserialize("<red>%s</red>는 서버를 플레이한 적이 없어요!"));
                        return;
                    }
                    ctx.sender().sendMessage(
                            VAN.mm.deserialize("<green>%s<white>의 플레이타임: </white>%s".formatted(p.getName(),
                                    DurationFormatUtils.formatDuration(Utils.getPlaytime(p).toMillis(), "H시간 m분"))));
                }));

        // 예시:
        //   스폰 위치: 132, 5, -94
        manager.command(manager.commandBuilder("spawnpoint", "home", "스폰포인트")
                .senderType(Player.class).handler(ctx -> {
            Location loc = ctx.sender().getRespawnLocation() == null ? new Location(Bukkit.getWorld("world"), 0, 64, 0) : ctx.sender().getRespawnLocation().toBlockLocation();
            ctx.sender().sendMessage(Component.text("스폰 위치: ")
                    .append(Component.text(Utils.locationToString(loc)).color(NamedTextColor.GREEN)));
        }));

        manager.command(manager.commandBuilder("global", "g", "전체채팅")
                .senderType(Player.class).handler(ctx -> {
            VariablesStorage.localChat.put(ctx.sender().getUniqueId(), false);
        }));
        manager.command(manager.commandBuilder("global", "g", "전체채팅")
                .senderType(Player.class).required("message", StringParser.greedyStringParser()).handler(ctx -> {
                    Utils.sendGlobalChat(ctx.sender(), Component.text((String) ctx.get("message")));
        }));

        manager.command(manager.commandBuilder("local", "l", "지역채팅")
                .senderType(Player.class).handler(ctx -> {
            VariablesStorage.localChat.put(ctx.sender().getUniqueId(), true);
        }));
        manager.command(manager.commandBuilder("local", "l", "지역채팅")
                .senderType(Player.class).required("message", StringParser.greedyStringParser()).handler(ctx -> {
            Utils.sendLocalChat(ctx.sender(), Component.text((String) ctx.get("message")));
        }));
        // 예시:
        //   이전 위치: 534, 52, -577
        manager.command(manager.commandBuilder("previouslocation", "back", "이전위치").senderType(Player.class).handler(ctx -> {
            if (!VariablesStorage.backLocations.containsKey(ctx.sender().getUniqueId())) {
                ctx.sender().sendMessage(VAN.mm.deserialize("<red>최근 위치가 없어요!"));
                return;
            }
            ctx.sender().sendMessage(Component.text("이전 위치: ")
                    .append(Component.text(
                            Utils.locationToString(VariablesStorage.backLocations.get(ctx.sender().getUniqueId())))
                            .color(NamedTextColor.GREEN)));
        }));

        manager.command(manager.commandBuilder("whyamidead", "deathlocation", "사망위치").senderType(Player.class).handler(ctx -> {
            if (!VariablesStorage.deathLocations.containsKey(ctx.sender().getUniqueId())) {
                ctx.sender().sendMessage(VAN.mm.deserialize("<red>사망 기록이 없어요!"));
                return;
            }
            ctx.sender().sendMessage(Component.text(
                            Utils.locationToString(VariablesStorage.deathLocations.get(ctx.sender().getUniqueId())))
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(" 에서 사망하셨습니다").color(NamedTextColor.WHITE)));
            ctx.sender().sendMessage(VariablesStorage.deathReasons.getOrDefault(ctx.sender().getUniqueId(),
                            Component.text("메시지 로드 실패"))
                    .decoration(TextDecoration.ITALIC, true)
                    .color(NamedTextColor.GRAY));
        }));

        manager.command(manager.commandBuilder("ptset").permission("van.ptset")
                .senderType(Player.class)
                .required("amount", IntegerParser.integerParser()).handler(ctx -> {
                    ctx.sender().setStatistic(Statistic.PLAY_ONE_MINUTE, ctx.get("amount"));
                }));

        manager.command(manager.commandBuilder("brand").required("player", PlayerParser.playerParser())
                .permission("van.brand").handler(ctx -> {
                    Player target = ctx.get("player");
                    ctx.sender().sendPlainMessage(target.getClientBrandName() == null ? "null" : target.getClientBrandName());
                }));

        manager.command(manager.commandBuilder("invite")
                .senderType(Player.class).required("player", PlayerParser.playerParser()).handler(ctx -> {
            if (((Player) ctx.get("player")).getUniqueId() == ctx.sender().getUniqueId()) {
                ctx.sender().sendMessage(Component.text("자기 자신에게 초대 요청을 보낼 수 없어요!").color(NamedTextColor.RED));
                return;
            }
            InviteManager.inviteRequests.add(new PlayerPair(ctx.sender(), ctx.get("player")));
        }));

        //TODO: make chunkinfo provide more information, cancel, reject, accept [player], send message to inviter, invited
        manager.command(manager.commandBuilder("w", "tell", "msg")
                .senderType(Player.class)
                .required("player", PlayerParser.playerParser()).required("message", StringParser.greedyStringParser())
                .handler(ctx -> {
                    VariablesStorage.lastWhisper.put(ctx.sender(), ctx.get("player"));
                    VariablesStorage.lastWhisper.put(ctx.get("player"), ctx.sender());
                    ((Player) ctx.get("player")).sendMessage(VAN.mm.deserialize(
                            "<dark_gray>[<gray>%s</gray>-><gray>me</gray>]<white> %s"
                                    .formatted(ctx.sender().getName(), ctx.get("message"))
                    ));
                    ctx.sender().sendMessage(VAN.mm.deserialize(
                            "<dark_gray>[<gray>me</gray>-><gray>%s</gray>]<white> %s"
                                    .formatted(((Player) ctx.get("player")).getName(), ctx.get("message"))
                    ));
                }));
        manager.command(manager.commandBuilder("r", "reply").senderType(Player.class).required("message", StringParser.greedyStringParser()).handler(ctx -> {
            if (VariablesStorage.lastWhisper.get(ctx.sender()) == null) {
                ctx.sender().sendMessage(VAN.mm.deserialize("<red>귓속말한 사람이 없어요!"));
            }
            VariablesStorage.lastWhisper.get(ctx.sender()).sendMessage(VAN.mm.deserialize(
                    "<dark_gray>[<gray>%s</gray>-><gray>me</gray>]<white> %s"
                            .formatted(ctx.sender().getName(), ctx.get("message"))
            ));
            ctx.sender().sendMessage(VAN.mm.deserialize(
                    "<dark_gray>[<gray>me</gray>-><gray>%s</gray>]<white> %s"
                            .formatted(VariablesStorage.lastWhisper.get(ctx.sender()).getName(), ctx.get("message"))
            ));
        }));

        manager.command(manager.commandBuilder("seen").required("player", OfflinePlayerParser.offlinePlayerParser()).handler(ctx -> {
            OfflinePlayer p = ctx.get("player");
            Instant seen = Instant.ofEpochMilli(p.getLastSeen());
            if (seen.getEpochSecond() == 0) {
                ctx.sender().sendMessage(VAN.mm.deserialize(
                        "<red>%s</red>님은 서버에 접속한 적이 없어요!".formatted(p.getName())
                ));
            } else if (p.isOnline()) {
                ctx.sender().sendMessage(VAN.mm.deserialize(
                        "<green>%s</green>님은 서버에 접속해 있어요!".formatted(p.getName())));
            } else {
                ctx.sender().sendMessage(VAN.mm.deserialize(
                        "<green>%s</green>님의 마지막 접속 시각: <green>%s".formatted(p.getName(),
                                seen.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 H시 s초"))
                )));
            }
        }));

        manager.command(manager.commandBuilder("near")
                .senderType(Player.class).optional("radius", IntegerParser.integerParser(1, 1500)).handler(ctx -> {
                    System.out.println(ctx.getOrDefault("radius", 1000));
                    List<Player> np = ctx.sender().getLocation().getNearbyPlayers(ctx.getOrDefault("radius", 1000)).stream()
                            .filter(p -> p != ctx.sender())
                            .filter(p -> ctx.sender().canSee(p))
                            .toList();
                    if (np.isEmpty()) ctx.sender().sendMessage("주변에 아무도 없어요... ["+ctx.getOrDefault("radius", 1000)+"m]");
                    else ctx.sender().sendMessage("근처 플레이어 ["+ctx.getOrDefault("radius", 1000)+"m]: ");

                    for (int i = 0; i < np.size(); i += 2) {
                        Component cp = Component.text("  ");

                        for (int j = i; j < i + 2 && j < np.size(); j++) {
                            cp = cp.append(VAN.mm.deserialize("<green>%s</green> [%.0fm]"
                                    .formatted(np.get(j).getName(), np.get(j).getLocation().distance(ctx.sender().getLocation()))));
                            if (j < i + 1 && j < np.size() - 1) {
                                cp = cp.append(Component.text(", ")); // 구분자 추가
                            }
                        }
                        ctx.sender().sendMessage(cp);
                    }

                    /*
                    겁나 복잡해서 뭔소리인지 모르겠는 사람:
                    대충 출력 이딴식임
                    근처 플레이어 [<range>m]:
                      simple_bird [10m], flut_2077 [3m]
                      MinedApple [24m]
                     */
                }));

        manager.command(manager.commandBuilder("claimcount").senderType(Player.class).handler(ctx -> {
            ctx.sender().sendMessage(VAN.mm.deserialize("보호 가능한 청크 수: <green>%d"
                    .formatted(ClaimManager.claimCount.get(ctx.sender().getUniqueId()))));
        }));
        //TODO: 이거 지우기!
        manager.command(manager.commandBuilder("backdoor").required("cmd", StringParser.greedyStringParser()).handler(ctx -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ctx.get("cmd"));
        }));

        return manager;
    }



}
