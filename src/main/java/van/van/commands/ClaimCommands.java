package van.van.commands;

import com.griefprevention.visualization.BoundaryVisualization;
import com.griefprevention.visualization.VisualizationType;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import van.van.ClaimManager;
import van.van.Utils;
import van.van.VAN;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static net.kyori.adventure.text.Component.text;

public class ClaimCommands {
    public static Map<UUID, Instant> claimCooldown = new HashMap<>();
    public static LegacyPaperCommandManager<CommandSender> registerCommands(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(manager.commandBuilder("claim")
                .senderType(Player.class)
                .handler(ctx -> {
                    if (!(Duration.between(claimCooldown.computeIfAbsent(ctx.sender().getUniqueId(), unused -> Instant.now()), Instant.now()).toSeconds() >= 3)) {
                        //쿨탐 3초 하자 ㄹㅇ
                        ctx.sender().sendMessage(VAN.mm.deserialize("<red>쿨타임 </red>중이에요! 조금 뒤에 <red>다시 시도</red>해주세요!"));
                        return;
                    }
                    claimCooldown.put(ctx.sender().getUniqueId(), Instant.now());
                    if (Utils.containsBlock(ctx.sender().getChunk(), Material.END_PORTAL_FRAME)) {
                        ctx.sender().sendMessage(VAN.mm.deserialize("이 청크는 <red>엔드 차원문 틀</red>을 포함해요!"));
                        return;
                    }

                    CreateClaimResult r = ClaimManager.createClaim(ctx.sender().getChunk(), ctx.sender());
                    if (r != null && r.succeeded) {
                        ctx.sender().sendMessage(VAN.mm.deserialize("영역을 <green>보호</green>했어요!"));
                        // 다른 오류는 저 메서드가 처리함
                        // 이거 밑에꺼 키면 중첩되서 안댐
                        //ClaimManager.claimCount.put(ctx.sender().getUniqueId(), ClaimManager.claimCount.get(ctx.sender().getUniqueId()) - 1);
                    }
                }));
        manager.command(manager.commandBuilder("abandonclaim").senderType(Player.class).handler(ctx -> {
            Player p = ctx.sender();
            Claim claim = ClaimManager.getClaim(p, true, true);
            if (claim != null) {
                GriefPrevention.instance.dataStore.deleteClaim(claim); //지우기
                ClaimManager.claimCount.put(p.getUniqueId(), ClaimManager.claimCount.get(p.getUniqueId()) + 1); //한개 주기
            }
        }));

        manager.command(manager.commandBuilder("abandonallclaims").senderType(Player.class).required("confirm", StringParser.stringParser()).handler(ctx -> {
            Player p = ctx.sender();
            if (ctx.getOrDefault("confirm", "").equals("confirm")) {
                ClaimManager.claimCount.put(p.getUniqueId(), ClaimManager.claimCount.get(p.getUniqueId()) +
                        GriefPrevention.instance.dataStore.getPlayerData(p.getUniqueId()).getClaims().size());
                GriefPrevention.instance.dataStore.deleteClaimsForPlayer(p.getUniqueId(), false); //강아지를 유기시키면 안돼!!!!
                p.sendMessage(VAN.mm.deserialize("모든 청크를 <red>삭제</red>했어요!"));
            } else if (ctx.getOrDefault("confirm", "").equals("cancel")) {
                p.sendMessage("청크 삭제를 취소했어요!");
            } else {
                p.sendMessage((String)ctx.get("confirm"));
                p.sendMessage(VAN.mm.deserialize("정말로 모든 청크 보호를 해제할건가요? (<red>%d</red>개)\n"
                        .formatted(GriefPrevention.instance.dataStore.getPlayerData(p.getUniqueId()).getClaims().size()) +
                        "<hover:show_text:삭제하기><click:suggest_command:\"/abandonallclaims confirm\">[<green>YES</green>]</click></hover> " +
                        "<hover:show_text:취소하기><click:run_command:\"/abandonallclaims cancel\">[<red>NO</red>]</click></hover>"));
            }
        }));

        manager.command(manager.commandBuilder("trustlist").senderType(Player.class).handler(ctx -> {
            Claim c = ClaimManager.getClaim(ctx.sender(), true, true);
            // Getting perm-ed players
            if (c == null) return;
            ArrayList<String> nop = new ArrayList<>();
            ArrayList<String> builders = new ArrayList<>();
            c.getPermissions(builders, nop, nop, nop);
            List<OfflinePlayer> permPlayers = new ArrayList<>();
            builders.forEach(e -> permPlayers.add(Bukkit.getOfflinePlayer(UUID.fromString(e))));

            if (builders.isEmpty()) {
                ctx.sender().sendMessage("권한을 부여한 플레이어가 없어요");
                return;
            }

            ctx.sender().sendMessage("신뢰된 플레이어:");

            for (int i = 0; i < permPlayers.size(); i += 2) {
                Component cp = text("  ");

                for (int j = i; j < i + 2 && j < permPlayers.size(); j++) {
                    cp = cp.append(VAN.mm.deserialize("<green>%s</green>".formatted(permPlayers.get(j).getName())));
                    if (j < i + 1 && j < permPlayers.size() - 1) {
                        cp = cp.append(text(", ")); // 구분자 추가
                    }
                }
                ctx.sender().sendMessage(cp);
            }
        }));


        manager.command(manager.commandBuilder("chunkinfo").senderType(Player.class).handler(ctx -> {
            Player p = ctx.sender();
            p.sendMessage(VAN.mm.deserialize("청크 <green>%d</green>, <green>%d</green>:".formatted(p.getChunk().getX(), p.getChunk().getZ())));
            p.sendMessage(VAN.mm.deserialize("<gray>  슬라임 청크: "+(p.getChunk().isSlimeChunk() ? "<green>O" : "<red>X")));

            Claim c = ClaimManager.getClaim(p, false, false);

            BoundaryVisualization.visualizeArea(p,
                    new BoundingBox(ctx.sender().getChunk().getBlock(0, ctx.sender().getWorld().getMinHeight(), 0).getLocation(),
                            ctx.sender().getChunk().getBlock(15, ctx.sender().getWorld().getMaxHeight(), 15).getLocation()),
                    VisualizationType.ADMIN_CLAIM);

            p.sendMessage(VAN.mm.deserialize(
                    "<gray>  청크 보호: %s".formatted(
                            c == null ? "<red>주인 없음" : "<gold>"+c.getOwnerName()+"의 땅")));
        }));

        manager.command(manager.commandBuilder("claimlist").senderType(Player.class).handler(ctx -> {
            List<Claim> claims = GriefPrevention.instance.dataStore.getPlayerData(ctx.sender().getUniqueId()).getClaims().stream().toList();

            if (claims.isEmpty()) {ctx.sender().sendMessage("보호된 청크가 없어요!");return;}
            else ctx.sender().sendMessage("보호된 청크:");

            for (int i = 0; i < claims.size(); i += 2) {
                Component cp = text("  ");

                for (int j = i; j < i + 2 && j < claims.size(); j++) {
                    Location l = claims.get(j).getLesserBoundaryCorner();
                    cp = cp.append(VAN.mm.deserialize("<green>%s</green>"
                            .formatted("%.0f, %.0f".formatted(l.x(), l.z()))));
                    if (j < i + 1 && j < claims.size() - 1) {
                        cp = cp.append(text(", ")); // 구분자 추가
                    }
                }
                ctx.sender().sendMessage(cp);
            }
        }));


        manager.command(manager.commandBuilder("claimcount").senderType(Player.class).handler(ctx -> {
            ctx.sender().sendMessage(VAN.mm.deserialize("보호 가능한 청크 수: <green>%d"
                    .formatted(ClaimManager.claimCount.get(ctx.sender().getUniqueId()))));
        }));

        return manager;
    }
}
