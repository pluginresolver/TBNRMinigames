package net.tbnr.minigame.ascension;

import net.tbnr.gearz.arena.Arena;
import net.tbnr.gearz.arena.ArenaField;
import net.tbnr.gearz.arena.ArenaIterator;
import net.tbnr.gearz.arena.Point;
import org.bukkit.World;

public class ASArena extends Arena {
    @ArenaField(longName = "Spawn Points", key = "spawn-points", loop = true, type = ArenaField.PointType.Player)
    public ArenaIterator<Point> spawnPoints;

    public ASArena(String name, String author, String description, String worldId, String id) {
        super(name, author, description, worldId, id);
    }

    public ASArena(String name, String author, String description, World world) {
        super(name, author, description, world);
    }
}
