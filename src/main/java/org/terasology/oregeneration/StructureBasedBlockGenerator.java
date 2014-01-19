package org.terasology.oregeneration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

    private Set<Block> blocksToReplace;
    private boolean debug;

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

        CollectingStructureCallback collectingStructureCallback = new CollectingStructureCallback(chunk);

        for (Structure structure : structures) {
            structure.generateStructure(collectingStructureCallback);
        }

        Map<Vector3i, Block> replacements = collectingStructureCallback.getReplacements();

        if (debug) {
            for (int x = 0; x < chunk.getChunkSizeX(); x++) {
                for (int y = 1; y < chunk.getChunkSizeY(); y++) {
                    for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                        Block block = replacements.get(new Vector3i(x, y, z));
                        if (block != null) {
                            chunk.setBlock(x, y, z, block);
                        } else {
                            chunk.setBlock(x, y, z, BlockManager.getAir());
                        }
                    }
                }
            }
        } else {
            for (Map.Entry<Vector3i, Block> replacementBlock : replacements.entrySet()) {
                chunk.setBlock(replacementBlock.getKey(), replacementBlock.getValue());
            }
        }
    }

    private class CollectingStructureCallback implements Structure.StructureCallback {
        private Chunk chunk;
        private Map<Vector3i, Block> replacements = Maps.newHashMap();
        private Map<Vector3i, Float> forces = Maps.newHashMap();

        private CollectingStructureCallback(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void replaceBlock(Vector3i position, float force, Block block) {
            Float oldForce = forces.get(position);
            if (oldForce == null || oldForce < force) {
                replacements.put(position, block);
            }
        }

        @Override
        public boolean canReplace(int x, int y, int z) {
            if (x < 0 || y < 1 || z < 0
                    || x >= chunk.getChunkSizeX() || y >= chunk.getChunkSizeY() || z >= chunk.getChunkSizeZ()) {
                return false;
            }
            return debug || blocksToReplace.contains(chunk.getBlock(x, y, z));
        }

        private Map<Vector3i, Block> getReplacements() {
            return replacements;
        }
    }

    public void addStructureGenerator(BlockStructureGenerator structureGenerator) {
        structureGenerators.add(structureGenerator);
    }
}