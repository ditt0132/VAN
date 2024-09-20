package van.van;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.UUID;

public class PlayerPair {
    private final Player p1;
    private final Player p2;
    private final String name1;
    private final String name2;
    private final UUID uuid1;
    private final UUID uuid2;
    private final Instant creationDate;

    /**
    메인쓰레드에서 생성하셈 가능하다면
    Player#getUniqueId, player#getName 호출함
     */
    public PlayerPair(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.name1 = p1.getName();
        this.name2 = p2.getName();
        this.uuid1 = p1.getUniqueId();
        this.uuid2 = p2.getUniqueId();
        this.creationDate = Instant.now();
    }

    public Player getLeft() {
        return p1;
    }
    public Player getRight() {
        return p2;
    }

    public UUID getLeftUUID() {
        return uuid1;
    }

    public UUID getRightUUID() {
        return uuid2;
    }

    public Instant getTimestamp() {
        return creationDate;
    }

    public String getLeftName() {
        return name1;
    }

    public String getRightName() {
        return name2;
    }
}
