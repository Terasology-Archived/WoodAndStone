package org.terasology.oregeneration;

import com.google.common.collect.Lists;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.FirstPassGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class StructureBasedBlockGenerator implements FirstPassGenerator {
    private String worldSeed;
    private WorldBiomeProvider worldBiomeProvider;
    private List<BlockStructureGenerator> structureGenerators = Lists.newLinkedList();
    private boolean debug;

    private Set<Block> blocksToReplace;

    public StructureBasedBlockGenerator(Set<Block> blocksToReplace) {
        this.blocksToReplace = blocksToReplace;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {
        worldBiomeProvider = biomeProvider;
    }

    @Override
    public void setWorldSeed(String seed) {
        this.worldSeed = seed;
    }

    @Override
    public void setInitParameters(Map<String, String> initParameters) {
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void generateChunk(Chunk chunk) {
        List<Structure> structures = new LinkedList<>();
        for (BlockStructureGenerator structureGenerator : structureGenerators) {
            List<Structure> generatedStructures = structureGenerator.generateStructures(chunk, worldSeed, worldBiomeProvider);
            if (generatedStructures != null) {
                structures.addAll(generatedStructures);
            }
        }

        if (debug) {
            for (int x = 0; x < chunk.getChunkSizeX(); x++) {
                for (int y = 0; y < chunk.getChunkSizeY(); y++) {
                    for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                        replaceBlock(chunk, structures, x, y, z);
                    }
                }
            }
        } else {
            for (int x = 0; x < chunk.getChunkSizeX(); x++) {
                for (int y = 0; y < chunk.getChunkSizeY(); y++) {
                    for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                        if (blocksToReplace.contains(chunk.getBlock(x, y, z))) {
                            replaceBlock(chunk, structures, x, y, z);
                        }
                    }
                }
            }
        }
    }

    private void replaceBlock(Chunk chunk, List<Structure> structures, int x, int y, int z) {
        Vector3i position = new Vector3i(x, y, z);
        Block blockToUse = null;
        float maxWeight = 0;
        for (Structure structure : structures) {
            float weightForStructure = structure.getWeightForChunkPosition(position);
            if (weightForStructure > maxWeight) {
                maxWeight = weightForStructure;
                blockToUse = structure.getBlockToPlaceInChunkPosition(position);
            }
        }

        if (blockToUse != null) {
            chunk.setBlock(x, y, z, blockToUse);
        } else if (debug && y > 0) {
            chunk.setBlock(x, y, z, BlockManager.getAir());
        }
    }

    public void addStructureGenerator(BlockStructureGenerator structureGenerator) {
        structureGenerators.add(structureGenerator);
    }
}