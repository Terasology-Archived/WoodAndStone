package org.terasology.anotherWorld;

import org.terasology.world.chunks.Chunk;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface LandscapeGenerator {
    public void initializeWithSeed(String seed);

    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation);
}
