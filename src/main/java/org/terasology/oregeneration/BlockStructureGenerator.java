package org.terasology.oregeneration;

import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.chunks.Chunk;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface BlockStructureGenerator {
    public List<Structure> generateStructures(Chunk chunk, String seed, WorldBiomeProvider worldBiomeProvider);
}
