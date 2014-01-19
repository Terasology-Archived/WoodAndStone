package org.terasology.oregeneration;

import org.terasology.math.Vector3i;
import org.terasology.oregeneration.util.PDist;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ClusterStructureGenerator extends AbstractBlockStructureGenerator {
    private static final int generatorSalt = 1978;

    private PDist frequency;
    private PDist pocketYLevel;
    private PDist clusterRichness;

    private Block block;

    public ClusterStructureGenerator(Block block, PDist frequency, PDist pocketYLevel, PDist clusterRichness) {
        this.block = block;
        this.frequency = frequency;
        this.pocketYLevel = pocketYLevel;
        this.clusterRichness = clusterRichness;
    }

    @Override
    public List<Structure> generateStructures(Chunk chunk, String seed, WorldBiomeProvider worldBiomeProvider) {
        Vector3i chunkPosition = chunk.getPos();
        Vector3i chunkSize = new Vector3i(chunk.getChunkSizeX(), chunk.getChunkSizeY(), chunk.getChunkSizeZ());

        List<Structure> result = new LinkedList<>();
        int chunksRangeToEvaluate = (int) Math.ceil(Math.max(clusterRichness.getMax() / chunk.getChunkSizeX(), clusterRichness.getMax() / chunk.getChunkSizeZ()));
        for (int chunkX = -chunksRangeToEvaluate; chunkX <= chunksRangeToEvaluate; chunkX++) {
            for (int chunkZ = -chunksRangeToEvaluate; chunkZ <= chunksRangeToEvaluate; chunkZ++) {
                generateStructuresForChunk(result, seed,
                        new Vector3i(
                                chunkPosition.x + chunkX, 0,
                                chunkPosition.z + chunkZ),
                        chunkSize, chunkX * chunk.getChunkSizeX(), chunkZ * chunk.getChunkSizeZ());
            }
        }

        return result;
    }

    private void generateStructuresForChunk(List<Structure> result, String seed, Vector3i chunkPosition, Vector3i chunkSize, int xShift, int zShift) {
        long seedForChunk = getSeedForChunk(seed, chunkPosition, generatorSalt);
        FastRandom random = new FastRandom(seedForChunk);

        float clustersInChunk = frequency.getValue(random);
        int clustersToGenerateInChunk = (int) clustersInChunk;

        // Check if we "hit" any leftover
        if (random.nextFloat() < clustersInChunk - clustersToGenerateInChunk) {
            clustersToGenerateInChunk++;
        }

        for (int i = 0; i < clustersToGenerateInChunk; i++) {
            // Location of cluster "center"
            float x = random.nextFloat() * chunkSize.x + xShift;
            float y = pocketYLevel.getValue(random);
            float z = random.nextFloat() * chunkSize.z + zShift;

            // choose segment length and horizontal angle from +Z axis
            int size = clusterRichness.getIntValue(random);
            double horizAngle = random.nextFloat() * Math.PI;
            float[] ptA = new float[3];
            float[] ptB = new float[3];

            // rotate segment in XZ plane
            float segmentXOffset = (float) Math.sin(horizAngle) * size / 8F;
            float segmentZOffset = (float) Math.cos(horizAngle) * size / 8F;
            ptA[0] = x + segmentXOffset;
            ptB[0] = x - segmentXOffset;
            ptA[2] = z + segmentZOffset;
            ptB[2] = z - segmentZOffset;

            // drop each endpoint randomly by up to 2 blocks
            // not sure why only negative changes are allowed, possibly
            // in deference to the upper height limits imposed on ore spawning.
            ptA[1] = y + random.nextInt(3) - 2;
            ptB[1] = y + random.nextInt(3) - 2;

            // compute radii along segment and expand BB
            // Radius has a random factor from [0,size/32] and a position factor that smoothly
            // increases the radius towards the center of the segment, up of a max of x2 at the
            // middle.
            float[] rad = new float[size + 1];
            for (int s = 0; s < rad.length; s++) {
                // radius for this step
                float ns = s / (float) (rad.length - 1);
                float baseRadius = (float) random.nextDouble() * size / 32F;
                rad[s] = ((float) Math.sin(ns * Math.PI) + 1) * baseRadius + 0.5F;
            }

            result.add(new ClusterStructure(chunkSize, rad, ptA, ptB));
        }
    }

    private class ClusterStructure implements Structure {
        private Vector3i chunkSize;
        private float[] rad;
        private float[] ptA;
        private float[] ptB;

        private ClusterStructure(Vector3i chunkSize, float[] rad, float[] ptA, float[] ptB) {
            this.chunkSize = chunkSize;
            this.rad = rad;
            this.ptA = ptA;
            this.ptB = ptB;
        }

        @Override
        public void generateStructure(StructureCallback callback) {
            // iterate along segment in unit steps
            for (int s = 0; s < rad.length; s++) {
                float ns = s / (float) (rad.length - 1);

                // center of sphere for this step
                float xCenter = ptA[0] + (ptB[0] - ptA[0]) * ns;
                float yCenter = ptA[1] + (ptB[1] - ptA[1]) * ns;
                float zCenter = ptA[2] + (ptB[2] - ptA[2]) * ns;

                // iterate over each block in the bounding box of the sphere
                int xMin = (int) Math.max(0, Math.floor(xCenter - rad[s]));
                int xMax = (int) Math.min(chunkSize.x - 1, Math.ceil(xCenter + rad[s]));
                int yMin = (int) Math.max(0, Math.floor(yCenter - rad[s]));
                int yMax = (int) Math.min(chunkSize.y - 1, Math.ceil(yCenter + rad[s]));
                int zMin = (int) Math.max(0, Math.floor(zCenter - rad[s]));
                int zMax = (int) Math.min(chunkSize.z - 1, Math.ceil(zCenter + rad[s]));
                for (int tgtX = xMin; tgtX <= xMax; tgtX++) {
                    double normXDist = (tgtX + 0.5D - xCenter) / rad[s];
                    if (normXDist * normXDist >= 1.0D) {
                        continue;
                    }
                    for (int tgtY = yMin; tgtY <= yMax; tgtY++) {
                        double normYDist = (tgtY + 0.5D - yCenter) / rad[s];
                        if (normXDist * normXDist + normYDist * normYDist >= 1.0D) {
                            continue;
                        }
                        for (int tgtZ = zMin; tgtZ <= zMax; tgtZ++) {
                            if (!callback.canReplace(tgtX, tgtY, tgtZ)) {
                                continue;
                            }
                            double normZDist = (tgtZ + 0.5D - zCenter) / rad[s];
                            if (normXDist * normXDist + normYDist * normYDist + normZDist * normZDist >= 1.0D) {
                                continue;
                            }
                            callback.replaceBlock(new Vector3i(tgtX, tgtY, tgtZ), 1, block);
                        }
                    }
                }
            }

        }
    }
}
