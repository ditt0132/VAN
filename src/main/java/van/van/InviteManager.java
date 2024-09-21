package van.van;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InviteManager {

    /* {Sender: [Target, RequestedTime] */
    public static Set<PlayerPair> inviteRequests = new HashSet<>();
    private static BukkitTask scheduler;
    public static void start() {
        if (scheduler != null) {
            VAN.instance.getLogger().warning("Scheduler already started! restarting scheduler");
            scheduler.cancel();
        }

        scheduler = Bukkit.getScheduler().runTaskTimerAsynchronously(VAN.instance, ()->{
            inviteRequests.forEach(p -> {
                if (p.getTimestamp().isAfter(Instant.now())) {
                    p.getLeft().sendMessage(VAN.mm.deserialize("<red>%s<white>에게 보낸 초대 요청이 <red>만료<white>됐어요!"));
                }
            });//1트만에 되면 팬티벗고 소리지름 ㄹㅇ
        }, 0L, 60L);
    }
}
