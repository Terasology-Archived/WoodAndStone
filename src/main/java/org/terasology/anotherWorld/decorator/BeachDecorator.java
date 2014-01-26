package org.terasology.anotherWorld.decorator;

import org.terasology.anotherWorld.BiomeProvider;
import org.terasology.anotherWorld.ChunkDecorator;
import org.terasology.anotherWorld.ChunkInformation;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BeachDecorator implements ChunkDecorator {
    private BlockFilter blockFilter;
    private Block beachBlock;
    private int aboveSeaLevel;
    private int belowSeaLevel;

    public BeachDecorator(BlockFilter blockFilter, Block beachBlock, int aboveSeaLevel, int belowSeaLevel) {
        this.blockFilter = blockFilter;
        this.beachBlock = beachBlock;
        this.aboveSeaLevel = aboveSeaLevel;
        this.belowSeaLevel = belowSeaLevel;
    }

    @Override
    public void initializeWithSeed(String seed) {
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation, int seaLevel, BiomeProvider biomeProvider) {
        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                int groundLevel = chunkInformation.getGroundLevel(x, z);
                if (groundLevel <= seaLevel + aboveSeaLevel && groundLevel >= seaLevel - belowSeaLevel) {
                    for (int y = seaLevel - belowSeaLevel; y < seaLevel + aboveSeaLevel; y++) {
                        if (blockFilter.accepts(chunk, x, y, z)) {
                            chunk.setBlock(x, y, z, beachBlock);
                        }
                    }
                }
            }
        }
    }
}
