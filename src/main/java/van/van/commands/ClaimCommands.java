package van.van.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import van.van.ClaimManager;

import java.util.Collection;

import static net.kyori.adventure.text.Component.text;

public class ClaimCommands {
    public static LegacyPaperCommandManager<CommandSender> registerCommands(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(manager.commandBuilder("claim")
                .senderType(Player.class)
                .handler(ctx -> {
                    CreateClaimResult r = ClaimManager.createClaim(ctx.sender().getChunk(), ctx.sender());
                    if (r != null && r.succeeded) {
                        ctx.sender().sendMessage(text("영역을 ").append(text("보호").color(NamedTextColor.GREEN))
                                .append(text("했어요!").color(NamedTextColor.WHITE)));
                        // "영역을 보호했어요!" 아오 ComponentAPI 보기 개불편하네
                    }
                }));
        manager.command(manager.commandBuilder("abandonclaim").senderType(Player.class).handler(ctx -> {
            Player p = ctx.sender();
            Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims(p.getChunk().getX(), p.getChunk().getZ());
            if (claims.size() > 1) { //1보다 크면
                p.sendMessage(text("심각한 문제가 발생했어요. 현재 좌표, 오류 내용과 함께 관리자에게 문의해주세요").decorate(TextDecoration.BOLD).color(NamedTextColor.DARK_RED).appendNewline()
                        .append(text("오류 내용: CLAIM_MULTIPLE_ON_CHUNK").decoration(TextDecoration.BOLD, false)));
            } else if (claims.isEmpty()) {
                p.sendMessage(text("현재 청크는 보호되어있지 않아요!").color(NamedTextColor.RED));
            } else {
                GriefPrevention.instance.dataStore.deleteClaim(claims.iterator().next()); //지우기
                ClaimManager.claimCount.put(p.getUniqueId(), ClaimManager.claimCount.get(p.getUniqueId()) + 1); //한개 주기
            }
        }));

        manager.command(manager.commandBuilder("chunkinfo").senderType(Player.class).handler(ctx -> {
            Player p = ctx.sender();
            MiniMessage mm = MiniMessage.miniMessage();

            p.sendMessage(mm.deserialize("청크 <green>%d</green>, <green>%d</green>:".formatted(p.getChunk().getX(), p.getChunk().getZ())));
            p.sendMessage(mm.deserialize("<gray>  슬라임 청크: "+(p.getChunk().isSlimeChunk() ? "<green>Yes" : "<red>No")));

            Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims(p.getChunk().getX(), p.getChunk().getZ());
            if (claims.size() > 1) { //1보다 크면
                p.sendMessage(text("심각한 문제가 발생했어요. 현재 좌표, 오류 내용과 함께 관리자에게 문의해주세요").decorate(TextDecoration.BOLD).color(NamedTextColor.DARK_RED).appendNewline()
                        .append(text("오류 내용: CLAIM_MULTIPLE_ON_CHUNK").decoration(TextDecoration.BOLD, false)));
            }

            p.sendMessage(mm.deserialize(
                    "<gray>  보호구역: %s".formatted(
                            claims.isEmpty() ? "<red>빈 땅" : "<gold>"+claims.iterator().next().getOwnerName()+"의 땅")));
        }));

        return manager;
    }
}
