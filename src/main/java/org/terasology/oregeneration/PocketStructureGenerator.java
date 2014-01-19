package org.terasology.oregeneration;

import org.terasology.math.Vector3i;
import org.terasology.oregeneration.util.NoiseGenerator;
import org.terasology.oregeneration.util.PDist;
import org.terasology.oregeneration.util.Transform;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PocketStructureGenerator extends AbstractBlockStructureGenerator {
    private static final int generatorSalt = 1410;

    private PDist frequency;

    private PDist pocketRadius;
    private PDist pocketThickness;
    private PDist pocketYLevel;
    private PDist pocketAngle;
    private PDist blockConcentration;
    private PDist blockDensity;
    private PDist noiseLevelsUsed;
    private PDist angleCutChance;

    private Block block;

    public PocketStructureGenerator(Block block, PDist frequency, PDist pocketYLevel, PDist pocketRadius, PDist pocketThickness, PDist pocketAngle,
                                    PDist blockDensity, PDist blockConcentration, PDist noiseLevelsUsed, PDist angleCutChance) {
        this.block = block;
        this.frequency = frequency;
        this.pocketYLevel = pocketYLevel;
        this.pocketRadius = pocketRadius;
        this.pocketThickness = pocketThickness;
        this.pocketAngle = pocketAngle;
        this.blockDensity = blockDensity;
        this.blockConcentration = blockConcentration;
        this.noiseLevelsUsed = noiseLevelsUsed;
        this.angleCutChance = angleCutChance;
    }

    @Override
    public List<Structure> generateStructures(Chunk chunk, String seed, WorldBiomeProvider worldBiomeProvider) {
        float maxRange = Math.max(pocketRadius.getMax(), pocketThickness.getMax());

        Vector3i chunkPosition = chunk.getPos();
        Vector3i chunkSize = new Vector3i(chunk.getChunkSizeX(), chunk.getChunkSizeY(), chunk.getChunkSizeZ());

        List<Structure> result = new LinkedList<>();
        int chunksRangeToEvaluate = (int) Math.ceil(Math.max(maxRange / chunk.getChunkSizeX(), maxRange / chunk.getChunkSizeZ()));
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

        float pocketsInChunk = frequency.getValue(random);
        int pocketsToGenerateInChunk = (int) pocketsInChunk;

        // Check if we "hit" any leftover
        if (random.nextFloat() < pocketsInChunk - pocketsToGenerateInChunk) {
            pocketsToGenerateInChunk++;
        }

        for (int i = 0; i < pocketsToGenerateInChunk; i++) {
            // Location of pocket "center"
            float x = random.nextFloat() * chunkSize.x + xShift;
            float y = pocketYLevel.getValue(random);
            float z = random.nextFloat() * chunkSize.z + zShift;

            // Pocket transformation matrix
            Transform pocketTransformation = new Transform();
            pocketTransformation.translate(x, y, z); // center translation
            pocketTransformation.rotateZInto(0, 1, 0); // rotate Z axis upward
            pocketTransformation.rotateZ(random.nextFloat() * 6.28319F); // phi rotation
            pocketTransformation.rotateY(pocketAngle.getValue(random)); // theta rotation

            pocketTransformation.scale(pocketRadius.getValue(random), pocketRadius.getValue(random), pocketThickness.getValue(random)); // scale axes

            Transform blockLookup = pocketTransformation.inverse();

            float[] origin = new float[]{x, y + 1, z};
            blockLookup.transformVector(origin);

            NoiseGenerator noise = new NoiseGenerator(random);

            int noiseLevelsToUse = noiseLevelsUsed.getIntValue(random);

            float maxRadius = Math.max(pocketRadius.getMax(), pocketThickness.getMax());

            result.add(new PocketStructure(noise, seedForChunk, blockLookup, new Vector3i(x, y, z), maxRadius, noiseLevelsToUse));
        }
    }

    private long getLocalizedSeed(long seed, Vector3i position) {
        return seed + 97231 * (31 * (31 * position.x + position.y) + position.z);
    }

    private class PocketStructure implements Structure {
        private NoiseGenerator noiseGenerator;
        private long seed;
        private Transform blockLookup;
        private Vector3i centerInChunkCoords;
        private float maxRadius;
        private int noiseLevelsToUse;

        private PocketStructure(NoiseGenerator noiseGenerator, long seed, Transform blockLookup,
                                Vector3i centerInChunkCoords, float maxRadius, int noiseLevelsToUse) {
            this.noiseGenerator = noiseGenerator;
            this.seed = seed;
            this.blockLookup = blockLookup;
            this.centerInChunkCoords = centerInChunkCoords;
            this.maxRadius = maxRadius;
            this.noiseLevelsToUse = noiseLevelsToUse;
        }

        @Override
        public float getWeightForChunkPosition(Vector3i position) {
            if (position.distance(centerInChunkCoords) > maxRadius + 1) {
                // Failed the simple distance check
                return 0;
            }

            FastRandom random = new FastRandom(getLocalizedSeed(seed, position));

            // Check density of the pocket
            if (blockDensity.getValue(random) < random.nextFloat()) {
                return 0;
            }

            // Check if within the pocket
            float[] positionTransformed = {position.x, position.y, position.z};
            blockLookup.transformVector(positionTransformed);
            float distanceFromOrigin =
                    (float) Math.sqrt(positionTransformed[0] * positionTransformed[0]
                            + positionTransformed[1] * positionTransformed[1]
                            + positionTransformed[2] * positionTransformed[2]);

            if (distanceFromOrigin > 1) {
                return 0;
            }


            // Check based on the concentration
            double nodeStrength = Math.pow(1 - distanceFromOrigin, blockConcentration.getValue(random));
            if (nodeStrength < random.nextFloat()) {
                return 0;
            }

            // Cut based on angle
            // Ensure no division by 0
            if (distanceFromOrigin == 0) {
                return (float) nodeStrength;
            }

            float noiseAtPointOnAngle = getNoise(positionTransformed[0] / distanceFromOrigin,
                    positionTransformed[1] / distanceFromOrigin, positionTransformed[2] / distanceFromOrigin);

            if ((noiseAtPointOnAngle + 1) / 2 < angleCutChance.getValue(random)) {
                // Noise angle tells us, there should be no block there
                return 0;
            }

            // This defines strength of the block existence there
            return (float) nodeStrength;
        }

        public float getNoise(float x, float y, float z) {
            double noise = 0;
            for (int i = 0; i < noiseLevelsToUse; i++) {
                float im = (1 << i);
                noise += (1 / im) * noiseGenerator.noise(x * im, y * im, z * im); // add 1/f noise
            }
            return (float) noise;
        }

        @Override
        public Block getBlockToPlaceInChunkPosition(Vector3i position) {
            return block;
        }
    }

}
