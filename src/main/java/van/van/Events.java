package van.van;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import io.papermc.paper.event.block.DragonEggFormEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class Events implements Listener {
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == TeleportCause.COMMAND
                || e.getCause() == TeleportCause.PLUGIN
                || e.getCause() == TeleportCause.UNKNOWN)
            VariablesStorage.backLocations.put(e.getPlayer().getUniqueId(), e.getFrom());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        VariablesStorage.deathLocations.put(e.getPlayer().getUniqueId(), e.getPlayer().getLastDeathLocation());
        VariablesStorage.deathReasons.put(e.getPlayer().getUniqueId(), e.deathMessage());
        if (e.getPlayer().getWorld().getEnvironment().equals(World.Environment.NORMAL)) { //is overworld?
            e.setKeepInventory(true); //keep inventory
            e.setKeepLevel(true);
            e.setDroppedExp(0);
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (VariablesStorage.localChat.getOrDefault(e.getPlayer().getUniqueId(), true)) {
            Utils.sendLocalChat(e.getPlayer(), e.message());
        } else {
            Utils.sendGlobalChat(e.getPlayer(), e.message());
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.HAND && e.getAction().isRightClick() && e.getItem() != null) {
            if (e.getItem().getType() == Material.GOLDEN_SHOVEL) {
                if (e.getClickedBlock() != null) return;
                e.getPlayer().performCommand("claim");
            } else if (e.getItem().getType() == Material.STICK) {
                e.getPlayer().performCommand("chunkinfo");
            }
        }
    }

    @EventHandler
    public void onDragonEggSpawn(DragonEggFormEvent e) {
        e.setCancelled(false);
    }

    @EventHandler
    public void onElytraBoost(PlayerElytraBoostEvent e) {
        if (e.getPlayer().getLocation().y() > e.getPlayer().getWorld().getMaxHeight() + 64) { //플레이어 위치가 최대높이+64보다 높으면
            e.setCancelled(true);
            e.setShouldConsume(false);
            e.getPlayer().sendMessage(VAN.mm.deserialize("<red>고도</red>가 너무 높아요!  최대 높이: <red>%d"
                    .formatted(e.getPlayer().getWorld().getMaxHeight() + 64)));
            e.getPlayer().setVelocity(new Vector(0, -10, 0)); //0, 0, 0
        }
    }

    @EventHandler
    public void onDragonBreak(EntityChangeBlockEvent e) {
        if (e.getEntityType().equals(EntityType.ENDER_DRAGON)) {
            e.setCancelled(true);
            System.out.print("ECB!");
        }
    }

    @EventHandler
    public void onDragonBreak(EntityExplodeEvent e) {
        if (e.getEntityType().equals(EntityType.ENDER_DRAGON)) {
            e.setCancelled(true);
        }
        //todo: 유효기간 시스템에 except 뭐하는건지 (see gp cfg ln 46),
        // 엔드 +-1024 뒤 재생성, 권한 체크, 엔더상점, 딱 한번 whyamidead 기록 안된이유
    }
}
