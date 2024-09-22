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

    private HashMap<UUID, Location> 이전위치 = new HashMap<>();
    private HashMap<UUID, Location> 사망위치 = new HashMap<>();
    private HashMap<UUID, Chunk> 보호된청크 = new HashMap<>();
    private HashMap<UUID, String> 채팅모드 = new HashMap<>();
    private HashMap<UUID, Long> 플레이타임 = new HashMap<>();
    private HashMap<UUID, Integer> 청크보호횟수 = new HashMap<>();
    private HashMap<UUID, Long> lastPlayTime = new HashMap<>();

    private HashMap<UUID, String> 사망이유 = new HashMap<>();
    private HashMap<UUID, Long> 마지막청크보호갱신시간 = new HashMap<>();
    private HashMap<UUID, Long> 마지막청크보호시간 = new HashMap<>();

    public static MiniMessage mm = MiniMessage.miniMessage();


    private File dataFile;
    private FileConfiguration dataConfig;
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
        //TODO: make saving system for VariablesStorage, ClaimManager maybe complete
        /*
        // OLD
        getServer().getPluginManager().registerEvents(this, this);
        loadData();
        startPlaytimeScheduler();
        startChunkProtectionScheduler();
        */
        //TODO: Deprecate this
    }

    @Override
    public void onDisable() {
        VariablesStorage.saveData();
        getLogger().info("저장 다했음 그리고 그거 비활성화함");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할수 있음 ㅗㅗㅗ.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        switch (command.getName().toLowerCase()) {
            case "스폰포인트": //DONE
                Location spawnLocation = player.getBedSpawnLocation();

                if (spawnLocation != null) {
                    player.sendMessage(ChatColor.GREEN + "님의 스폰포인트는 이거임: X: " + spawnLocation.getBlockX() + " Y: " + spawnLocation.getBlockY() + " Z: " + spawnLocation.getBlockZ());
                } else {
                    player.sendMessage(ChatColor.RED + "스폰 포인트가 설정되어 있찌않음");
                }
                break;

            case "이전위치": //DONE
                if (이전위치.containsKey(playerId)) {
                    player.teleport(이전위치.get(playerId));
                    player.sendMessage(ChatColor.GREEN + "이전 위치로 순간이동했어여"); // TP 아닌데 ㅋ
                } else {
                    player.sendMessage(ChatColor.RED + "이전 위치가 없수빈다");
                }
                break;

            case "사망이유": //DONE
                if (사망위치.containsKey(playerId)) {
                    Location deathLoc = 사망위치.get(playerId);
                    String deathReason = 사망이유.getOrDefault(playerId, "알 수 없는 이유");

                    player.sendMessage(ChatColor.RED + "당신은 여기서 뒤짐: X: " + deathLoc.getBlockX() + " Y: " + deathLoc.getBlockY() + " Z: " + deathLoc.getBlockZ());
                    player.sendMessage(ChatColor.RED + "죽은 이유: " + deathReason);
                } else {
                    player.sendMessage(ChatColor.RED + "알수없어여.");
                }
                break;

            case "초대":
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "플레이어 이름을 입력하세요.");
                } else {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        target.sendMessage(ChatColor.YELLOW + player.getName() + "님이 초대했습니다 /수락 또는 /거절 명령어로 응답하세요");
                        player.sendMessage(ChatColor.GREEN + "초대가 전송되었습니다.");
                    } else {
                        player.sendMessage(ChatColor.RED + "해당 플레이어를 찾을 수 없습니다.");
                    }
                }
                break;

            case "수락":
                player.sendMessage(ChatColor.GREEN + "초대를 수락했습니다.");
                break;

            case "거절":
                player.sendMessage(ChatColor.RED + "초대를 거절했습니다.");
                break;

            case "claim":
                Chunk chunk = player.getLocation().getChunk();
                if (청크보호횟수.getOrDefault(playerId, 0) > 0) {
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

            case "abandonclaim":
                if (보호된청크.containsKey(playerId)) {
                    보호된청크.remove(playerId);
                    player.sendMessage(ChatColor.GREEN + "청크 보호를 해제했수빈다");
                } else {
                    player.sendMessage(ChatColor.RED + "보호된 청크가 없습니다");
                }
                break;

            case "claims":
                if (보호된청크.containsKey(playerId)) {
                    Chunk protectedChunk = 보호된청크.get(playerId);
                    player.sendMessage(ChatColor.GREEN + "이거 뭐 어케만드는지 모르겠음 ㅗㅗ: X: " + protectedChunk.getX() + ", Z: " + protectedChunk.getZ());
                } else {
                    player.sendMessage(ChatColor.RED + "보호된 청크가 없습니다");
                }
                break;

            case "claiminfo":
                Chunk currentChunk = player.getLocation().getChunk();
                if (보호된청크.containsValue(currentChunk)) {
                    player.sendMessage(ChatColor.GREEN + "이 청크는 보호된 상태입니당");
                } else {
                    player.sendMessage(ChatColor.RED + "이 청크는 보호되지 않았습니담");
                }
                break;

            case "플레이타임"://DONE
                long timePlayed = 플레이타임.getOrDefault(playerId, 0L);
                long seconds = timePlayed / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                minutes %= 60;
                seconds %= 60;

                player.sendMessage(ChatColor.GREEN + "님의 플레이타임: " + hours + "시간 " + minutes + "분 " + seconds + "초");
                break;

            case "플레이타임제거": //DONE
                if (player.hasPermission("내플그.admin")) {
                    플레이타임.clear();
                    player.sendMessage(ChatColor.GREEN + "모든 플레이어의 플레이타임이 초기화되었습니다h");
                } else {
                    player.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다 그니까 쳐하지마세여");
                }
                break;


            case "플레이타임추가": //DONE
                if (sender.hasPermission("playtime.add")) {
                    if (args.length != 2) {
                        player.sendMessage(ChatColor.RED + "사용법: /플레이타임추가 <플레이어명> <초>");
                        return true;
                    }

                    Player target = getServer().getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "해당 플레이어를 찾을 수 없습니다.");
                        return true;
                    }

                    long additionalSeconds;
                    try {
                        additionalSeconds = Long.parseLong(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "초는 숫자로 입력해야 합니다.");
                        return true;
                    }

                    UUID targetId = target.getUniqueId();
                    long currentTime = 플레이타임.getOrDefault(targetId, 0L);
                    플레이타임.put(targetId, currentTime + additionalSeconds * 1000L);

                    target.sendMessage(ChatColor.GREEN + "관리자놈에 의해 " + additionalSeconds + "초가 추가되었습니다");
                    player.sendMessage(ChatColor.GREEN + "플레이타임이 " + additionalSeconds + "초 추가되었습니다");
                } else {
                    player.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다 그니까 좀 하지마");
                }
                break;

            case "플레이타임보상리셋": //?
                청크보호횟수.put(playerId, 0);
                마지막청크보호시간.put(playerId, System.currentTimeMillis());
                player.sendMessage(ChatColor.GREEN + "플레이타임 보상이 리셋되었습니다.");
                break;


            case "로컬채팅": //DONE
                String currentMode = 채팅모드.getOrDefault(playerId, "global");
                if ("local".equals(currentMode)) {
                    // 현재 로컬 채팅 모드인 경우 꺼지도록 설정
                    채팅모드.put(playerId, "global");
                    player.sendMessage(ChatColor.GREEN + "로컬 채팅 모드가 꺼졌습니다");
                } else {
                    // 현재 글로벌 채팅 모드인 경우 로컬 채팅 모드로 변경
                    채팅모드.put(playerId, "local");
                    player.sendMessage(ChatColor.GREEN + "로컬 채팅 모드가 켜졌습니다");
                }
                break;

            default:
                return false;
        }

        return true;
    }


    private void sendLocalChat(Player sender, String message) {
        double radius = 300.0;
        Location senderLoc = sender.getLocation();
        List<Player> nearbyPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(sender.getWorld()) && p.getLocation().distance(senderLoc) <= radius)
                .collect(Collectors.toList());


        for (Player player : nearbyPlayers) {
            player.sendMessage(ChatColor.DARK_GREEN + "[L] " + ChatColor.RESET + sender.getName() + ": " + message);
        }
    }

    private void sendGlobalChat(Player sender, String message) {
        Bukkit.getOnlinePlayers().forEach(player ->
                player.sendMessage(ChatColor.YELLOW + "[글로벌] " + ChatColor.RESET + sender.getName() + ": " + message)
        );
    }

    private void startPlaytimeScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    long currentTime = System.currentTimeMillis();
                    long lastPlayTime = 플레이타임.getOrDefault(playerId, currentTime);
                    long newPlayTime = 플레이타임.getOrDefault(playerId, 0L) + 1000; // 1초 증가
                    플레이타임.put(playerId, newPlayTime);
                }
            }
        }.runTaskTimer(this, 0L, 20L); // 1초마다 (20틱)
    }


    private void startChunkProtectionScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    long playtimeMillis = 플레이타임.getOrDefault(playerId, 0L);
                    int playtimeHours = (int) (playtimeMillis / (1000 * 60 * 60));

                    long lastUpdate = 마지막청크보호갱신시간.getOrDefault(playerId, 0L);
                    int currentProtectionCount = 청크보호횟수.getOrDefault(playerId, 0);

                    if (playtimeHours > (currentProtectionCount + 1) && (playtimeMillis - lastUpdate >= 3600000)) {
                        청크보호횟수.put(playerId, currentProtectionCount + 1);
                        마지막청크보호갱신시간.put(playerId, playtimeMillis);
                        player.sendMessage(ChatColor.GREEN + "접속시간 한시간마다 청크보호 횟수가 한개 생겼습니다! 현재 보호 횟수: " + (currentProtectionCount + 1));
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }


    private void saveData() {
        for (UUID playerId : 보호된청크.keySet()) {
            Chunk chunk = 보호된청크.get(playerId);
            dataConfig.set(playerId.toString() + ".chunk.x", chunk.getX());
            dataConfig.set(playerId.toString() + ".chunk.z", chunk.getZ());
            dataConfig.set(playerId.toString() + ".protectionCount", 청크보호횟수.getOrDefault(playerId, 0));
            dataConfig.set(playerId.toString() + ".lastProtectionTime", 마지막청크보호시간.getOrDefault(playerId, 0L));
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            saveResource("data.yml", false);
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String key : dataConfig.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            int x = dataConfig.getInt(key + ".chunk.x");
            int z = dataConfig.getInt(key + ".chunk.z");
            Chunk chunk = Bukkit.getWorlds().get(0).getChunkAt(x, z);
            보호된청크.put(playerId, chunk);
            청크보호횟수.put(playerId, dataConfig.getInt(key + ".protectionCount"));
            마지막청크보호시간.put(playerId, dataConfig.getLong(key + ".lastProtectionTime"));
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        if (보호된청크.containsValue(chunk) && !보호된청크.containsKey(player.getUniqueId()) && !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "이 청크는 보호된 청크입니다");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        if (보호된청크.containsValue(chunk) && !보호된청크.containsKey(player.getUniqueId()) && !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "이 청크는 보호된 청크입니다");
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();

        if (채팅모드.getOrDefault(playerId, "global").equals("local")) {
            sendLocalChat(player, message);
            event.setCancelled(true);
        } else {
            event.setFormat(ChatColor.GRAY + "[글로벌] " + ChatColor.RESET + "%1$s: %2$s");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID playerId = event.getEntity().getUniqueId();
        Location deathLocation = event.getEntity().getLocation();
        String deathMessage = event.getDeathMessage() != null ? event.getDeathMessage() : "알 수 없는 이유";

        사망위치.put(playerId, deathLocation);
        사망이유.put(playerId, deathMessage);
    }
}