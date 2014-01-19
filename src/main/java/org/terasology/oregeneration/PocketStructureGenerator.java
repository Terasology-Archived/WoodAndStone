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

    private Block block;

    private PDist frequency;

    private PDist pocketRadius;
    private PDist pocketThickness;
    private PDist pocketYLevel;
    private PDist pocketAngle;

    private PDist blockRadiusMult;
    private PDist blockDensity;

    private PDist noiseLevel;
    private PDist volumeNoiseCutOff;

    public PocketStructureGenerator(Block block, PDist frequency, PDist pocketRadius, PDist pocketThickness, PDist pocketYLevel, PDist pocketAngle,
                                    PDist blockRadiusMult, PDist blockDensity, PDist noiseLevel, PDist volumeNoiseCutOff) {
        this.block = block;
        this.frequency = frequency;
        this.pocketRadius = pocketRadius;
        this.pocketThickness = pocketThickness;
        this.pocketYLevel = pocketYLevel;
        this.pocketAngle = pocketAngle;
        this.blockRadiusMult = blockRadiusMult;
        this.blockDensity = blockDensity;
        this.noiseLevel = noiseLevel;
        this.volumeNoiseCutOff = volumeNoiseCutOff;
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

            if (pocketTransformation.determinant() == 0) {
                continue;
            }

            NoiseGenerator noise = new NoiseGenerator(random);
            float sizeNoiseMagnitude = Math.abs(noiseLevel.getValue(random));

            // build transformed bounding box from the local BB for a unit sphere
            float rMax = (1 + sizeNoiseMagnitude * 2) * blockRadiusMult.getMax();
            if (rMax < 0) {
                rMax = 0;
            }
            float[] bb = new float[]{-rMax, -rMax, -rMax, rMax, rMax, rMax};
            pocketTransformation.transformBB(bb);

            float minX = Math.min(bb[0], bb[3]);
            float minY = Math.min(bb[1], bb[4]);
            float minZ = Math.min(bb[2], bb[5]);

            Vector3i minPosition = new Vector3i(minX, minY, minZ);

            float maxX = Math.max(bb[0], bb[3]);
            float maxY = Math.max(bb[1], bb[4]);
            float maxZ = Math.max(bb[2], bb[5]);

            Vector3i maxPosition = new Vector3i(maxX + 1, maxY + 1, maxZ + 1);

            // calculate noise levels from size of BB
            float maxSize = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
            int noiseLevels = (maxSize <= 1) ? 0 : (int) (Math.log(maxSize) / Math.log(2) + 0.5F);

            Transform blockLookup = pocketTransformation.inverse();

            result.add(new PocketStructure(noise, seedForChunk, noiseLevels, sizeNoiseMagnitude, chunkSize, chunkPosition, minPosition, maxPosition, blockLookup));
        }
    }

    private long getLocalizedSeed(long seed, Vector3i position) {
        return seed + 97231 * (31 * (31 * position.x + position.y) + position.z);
    }

    private class PocketStructure implements Structure {
        private NoiseGenerator noiseGenerator;
        private long seed;
        private int noiseLevels;
        private Vector3i chunkPosition;
        private float sizeNoiseMagnitude;
        private Vector3i minPosition;
        private Vector3i chunkSize;
        private Vector3i maxPosition;
        private Transform invMat;

        private PocketStructure(NoiseGenerator noiseGenerator, long seed, int noiseLevels, float sizeNoiseMagnitude,
                                Vector3i chunkSize, Vector3i chunkPosition, Vector3i minPosition, Vector3i maxPosition, Transform invMat) {
            this.noiseGenerator = noiseGenerator;
            this.seed = seed;
            this.noiseLevels = noiseLevels;
            this.sizeNoiseMagnitude = sizeNoiseMagnitude;
            this.chunkSize = chunkSize;
            this.chunkPosition = chunkPosition;
            this.minPosition = minPosition;
            this.maxPosition = maxPosition;
            this.invMat = invMat;
        }

        /**
         * Get total 1/f noise value at the specified position
         */
        public float getNoise(float x, float y, float z) {
            double noise = 0;
            for (int i = 0; i < noiseLevels; i++) {
                float im = (1 << i);
                noise += (1 / im) * noiseGenerator.noise(x * im, y * im, z * im); // add 1/f noise
            }
            return (float) noise;
        }

        @Override
        public void generateStructure(StructureCallback callback) {
            FastRandom random = new FastRandom(getLocalizedSeed(seed, chunkPosition));

            // get min & max radii in local coordinates
            float maxR = Math.max(blockRadiusMult.getMax(), 0); // maximum radius after noise scaling
            float minR = Math.max(blockRadiusMult.getMin(), 0); // minimum radius after noise scaling
            float maxNoisyR2 = maxR * (1 + sizeNoiseMagnitude * 2); // maximum radius before noise scaling
            float minNoisyR2 = minR * (1 - sizeNoiseMagnitude * 2); // minimum radius before noise scaling
            maxNoisyR2 *= maxNoisyR2;
            minNoisyR2 *= minNoisyR2;
            // iterate through blocks
            float[] pos = new float[3];
            for (int x = Math.max(0, minPosition.x); x <= Math.min(chunkSize.x - 1, maxPosition.x); x++) {
                for (int y = Math.max(0, minPosition.y); y <= Math.min(chunkSize.y - 1, maxPosition.y); y++) {
                    for (int z = Math.max(0, minPosition.z); z <= Math.min(chunkSize.z - 1, maxPosition.z); z++) {
                        if (!callback.canReplace(x, y, z)) {
                            continue;
                        }
                        // transform into local coordinates
                        pos[0] = x + 0.5F;
                        pos[1] = y + 0.5F;
                        pos[2] = z + 0.5F;
                        invMat.transformVector(pos);
                        // check radius
                        float r2 = pos[0] * pos[0] + pos[1] * pos[1] + pos[2] * pos[2];
                        if (r2 > maxNoisyR2) {
                            continue; // block is outside maximum possible radius
                        }
                        if (r2 > minNoisyR2) // block is within max noise tolerance
                        {
                            // compute radius noise multiplier
                            // the point is projected radially onto the surface of the unit sphere and
                            // the 3D noise field is sampled at that location to determine the radius
                            // multiplier for that solid angle.
                            float r = (float) Math.sqrt(r2);
                            float mult = 1;
                            if (r > 0) {
                                mult += sizeNoiseMagnitude * getNoise(pos[0] / r, pos[1] / r, pos[2] / r);
                            } else {
                                mult += sizeNoiseMagnitude * getNoise(0, 0, 0);
                            }
                            if (mult <= 0) {
                                continue; // noise-multiplied radius at this solid angle is zero
                            }
                            r /= mult;
                            // check noise-scaled radius
                            if (r > maxR) {
                                continue; // unit radius is outside max cutoff
                            }
                            if (r > minR && r > blockRadiusMult.getValue(random)) {
                                continue; // block is outside cutoff
                            }
                        }
                        // apply internal density noise
                        if (volumeNoiseCutOff.getMin() > 1) {
                            continue; // noise cutoff is too high
                        } else if (volumeNoiseCutOff.getMax() > 0) {
                            if ((getNoise(pos[0], pos[1], pos[2]) + 1) / 2 < volumeNoiseCutOff.getValue(random)) {
                                continue; // noise level below cutoff
                            }
                        }
                        if (blockDensity.getIntValue(random) < 1) {
                            continue; // density check failed
                        }
                        // place block
                        callback.replaceBlock(new Vector3i(x, y, z), 1, block);
                    }
                }
            }

        }

    }

}
