package van.van;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import van.van.commands.ClaimCommands;
import van.van.commands.Commands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class VAN extends JavaPlugin implements Listener {

    public static MiniMessage mm = MiniMessage.miniMessage();
    public static VAN instance;

    @Override
    public void onEnable() {
        instance = this;

        // NEW
        getServer().getPluginManager().registerEvents(new Events(), this);
        VariablesStorage.loadData();
        LegacyPaperCommandManager<CommandSender> mgr = LegacyPaperCommandManager
                .createNative(this, ExecutionCoordinator.simpleCoordinator());

        Commands.registerCommands(mgr);
        ClaimCommands.registerCommands(mgr);
        ClaimManager.registerScheduler();
        getLogger().info("Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }
}