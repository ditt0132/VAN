package van.van.commands;

import cloud.commandframework.paper.PaperCommandManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class ClaimCommands {
    public PaperCommandManager<Player> registerCommands(PaperCommandManager<Player> manager) {
        manager.command(manager.commandBuilder("claim")
                .handler(ctx -> {
                    Player p = ctx.getSender();
                    Chunk chunk = p.getLocation().getChunk();
                    if (ClaimManager.claims.getOrDefault(playerId, 0) > 0) {
                        if (보호된청크.values().contains(chunk)) {
                            player.sendMessage(ChatColor.RED + "이 청크는 이미 보호된 청크임");
                        } else {
                            보호된청크.put(playerId, chunk);
                            청크보호횟수.put(playerId, 청크보호횟수.get(playerId) - 1);
                            player.sendMessage(ChatColor.GREEN + "현재 청크를 보호했어요 남은 보호 횟수: " + 청크보호횟수.get(playerId));
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "청크 보호 횟수가 부족해여");
                    }
                    break;
                }));
    }
}
