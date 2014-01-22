package org.terasology.anotherWorld.decorator;

import org.terasology.anotherWorld.BiomeProvider;
import org.terasology.anotherWorld.ChunkDecorator;
import org.terasology.anotherWorld.ChunkInformation;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

import java.util.Collection;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BeachDecorator implements ChunkDecorator {
    private Collection<Block> replacedBlocks;
    private Block beachBlock;

    public BeachDecorator(Collection<Block> replacedBlocks, Block beachBlock) {
        this.replacedBlocks = replacedBlocks;
        this.beachBlock = beachBlock;
    }

    @Override
    public void initializeWithSeed(String seed) {
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation, BiomeProvider biomeProvider, int seaLevel) {
        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                int groundLevel = chunkInformation.getGroundLevel(x, z);
                if (groundLevel <= seaLevel + 2 || groundLevel >= seaLevel - 2) {
                    for (int y = seaLevel - 2; y < seaLevel + 2; y++) {
                        if (replacedBlocks.contains(chunk.getBlock(x, y, z))) {
                            chunk.setBlock(x, y, z, beachBlock);
                        }
                    }
                }
            }
        }
    }
}
