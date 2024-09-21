package van.van;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimManager {
    // 주의: 만들어진 수 아니고 클레임 만들 수 있는 수임 ㅅㄱ
    public static Map<UUID, Integer> claimCount = new HashMap<>();
    public static Map<UUID, Duration> lastRewardedTimes = new HashMap<>();


    public static @Nullable CreateClaimResult createClaim(Chunk c, Player p) {
        // 있으면 반환 없으면 1 주고 반환 :>
        Integer count = claimCount.computeIfAbsent(p.getUniqueId(), k -> 1); // 기본으로 1청크를 주는 아주 자비로운 곳이랍니다^^
        if (count == 0) {
            p.sendMessage(Component.text("더이상 영역 보호를 할 수 없어요!").color(NamedTextColor.RED).appendNewline()
                    .append(Component.text("Tip: 한시간마다 영역보호 횟수 한개가 늘어나요").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)));
            return null;
        }
        Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims(c.getX(), c.getZ());
        if (claims.size() > 1) { //1보다 크면
            p.sendMessage(Component.text("심각한 문제가 발생했어요. 현재 좌표, 오류 내용과 함께 관리자에게 문의해주세요").decorate(TextDecoration.BOLD).color(NamedTextColor.DARK_RED).appendNewline()
                    .append(Component.text("오류 내용: CLAIM_MULTIPLE_ON_CHUNK").decoration(TextDecoration.BOLD, false)));
            return null;
        } else if (!claims.isEmpty()) {
            p.sendMessage(Component.text("이 청크에 이미 영역이 존재해요!").color(NamedTextColor.RED));
            if (claims.iterator().next().getOwnerID() == p.getUniqueId())
                p.sendMessage(Component.text("Tip: 본인의 영역이에요!")
                        .color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
            else
                p.sendMessage(Component.text("소유자: ")
                    .append(Component.text(claims.iterator().next().getOwnerName()).color(NamedTextColor.GREEN)));
            return null;
        }

        CreateClaimResult r = GriefPrevention.instance.dataStore.createClaim(
            c.getWorld(), c.getBlock(0, -40, 0).getX(), c.getBlock(15, 320, 15).getX(),
            c.getBlock(0, -40, 0).getY(), c.getBlock(15, 320, 15).getY(),
            c.getBlock(0, -40, 0).getZ(), c.getBlock(15, 320, 15).getZ(),
            p.getUniqueId(),
            null,
            null,
            p
        );
        if (r.succeeded) {
            claimCount.put(p.getUniqueId(), claimCount.get(p.getUniqueId()) - 1); //한개 차감
            return r;
        } else {
            // 나중의 나를 위해
            // CEXIST = parent 관련(불가능), 다른 영역과 겹치지만 위에서 예외처리되지 않음
            // CNULL = API를 사용하는 다른 플러그인때문에 취소됨, 월드가드 권한이 없음. 둘다 우리 서버의 경우에서는 말이 안되는 상황
            p.sendMessage(Component.text("심각한 문제가 발생했어요. 현재 좌표, 오류 내용과 함께 관리자에게 문의해주세요").decorate(TextDecoration.BOLD).color(NamedTextColor.DARK_RED).appendNewline()
                    .append(Component.text("오류 내용: CLAIM_UNSUCCESSFUL_"+(r.claim == null ? "CNULL" : "CEXIST")
                    ).decoration(TextDecoration.BOLD, false)));
            return null;
        }
    }

    public static void registerScheduler() {
        Bukkit.getScheduler().runTaskTimer(VAN.instance, ()->{
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (Utils.getPlaytime(p)
                        .minus(lastRewardedTimes.getOrDefault(p.getUniqueId(), Duration.ZERO))
                        .compareTo(Duration.ofHours(1)) >= 0) { //마지막 지급으로부터 1시간이 경과했으면
                    claimCount.put(p.getUniqueId(), claimCount.getOrDefault(p.getUniqueId(), 1) + 1);
                    lastRewardedTimes.put(p.getUniqueId(), Duration.ZERO);
                }
                lastRewardedTimes.put(p.getUniqueId(),
                        lastRewardedTimes.getOrDefault(p.getUniqueId(), Duration.ZERO).plusMinutes(1));
            }
        }, 0, 1200); //1분
        /*
        로직 설명:
        마지막 지급 시간으로부터 1시간이 경과했으면:
          클레임 갯수를 추가함
          지급 시간을 리셋함(0)
        Finally (무조건):
          마지막 지급 시간에 1분을 추가함
         */
    }

}
