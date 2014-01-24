/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.anotherWorld.decorator.structure;

import org.terasology.anotherWorld.util.NoiseGenerator;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.anotherWorld.util.Transform;
import org.terasology.math.Vector3i;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PocketStructureDefinition extends AbstractMultiChunkStructureDefinition {
    private PocketBlockProvider blockProvider;
    private PDist pocketRadius;
    private PDist pocketThickness;
    private PDist pocketYLevel;
    private PDist pocketAngle;
    private PDist blockRadiusMult;
    private PDist blockDensity;
    private PDist noiseLevel;
    private PDist volumeNoiseCutOff;

    public PocketStructureDefinition(PocketBlockProvider blockProvider, PDist frequency, PDist pocketRadius, PDist pocketThickness, PDist pocketYLevel, PDist pocketAngle,
                                     PDist blockRadiusMult, PDist blockDensity, PDist noiseLevel, PDist volumeNoiseCutOff) {
        super(frequency);
        this.blockProvider = blockProvider;
        this.pocketRadius = pocketRadius;
        this.pocketThickness = pocketThickness;
        this.pocketYLevel = pocketYLevel;
        this.pocketAngle = pocketAngle;
        this.blockRadiusMult = blockRadiusMult;
        this.blockDensity = blockDensity;
        this.noiseLevel = noiseLevel;
        this.volumeNoiseCutOff = volumeNoiseCutOff;
    }

    protected float getMaxRange() {
        return Math.max(pocketRadius.getMax(), pocketThickness.getMax());
    }

    @Override
    protected int getGeneratorSalt() {
        return 3423876;
    }

    @Override
    protected void generateStructuresForChunk(List<Structure> result, Random random, Vector3i chunkSize, int xShift, int zShift) {
        // cloud X,Y,Z coordinates within chunk
        float clX = random.nextFloat() * chunkSize.x + xShift;
        float clY = pocketYLevel.getValue(random);
        float clZ = random.nextFloat() * chunkSize.z + zShift;

        // cloud transformation matrix
        Transform clMat = new Transform();
        clMat.translate(clX, clY, clZ); // center translation
        clMat.rotateZInto(0, 1, 0); // rotate Z axis upward
        clMat.rotateZ(random.nextFloat() * 6.28319F); // phi rotation
        clMat.rotateY(pocketAngle.getValue(random)); // theta rotation
        clMat.scale(pocketRadius.getValue(random), pocketRadius.getValue(random), pocketThickness.getValue(random)); // scale axes

        // create cloud component
        result.add(new DiffusePocketStructure(clMat, random, chunkSize));
    }

    public interface PocketBlockProvider {
        public Block getBlock(float distanceFromCenter);
    }

    /**
     * A diffuse cloud of ore.
     */
    private class DiffusePocketStructure implements Structure {
        // transformation matrices
        protected final Transform mat;
        protected final Transform invMat;
        // noise generator
        protected final NoiseGenerator noiseGen;
        protected final float sizeNoiseMagnitude;
        protected final int noiseLevels;
        private Vector3i minPosition;
        private Vector3i maxPosition;
        private Random random;
        private Vector3i chunkSize;

        public DiffusePocketStructure(Transform transform, Random random, Vector3i chunkSize) {
            this.random = random;
            this.chunkSize = chunkSize;
            // create noise generator
            noiseGen = new NoiseGenerator(random);
            sizeNoiseMagnitude = Math.abs(noiseLevel.getValue(random));

            // build transformed bounding box from the local BB for a unit sphere
            float rMax = (1 + sizeNoiseMagnitude * 2) * blockRadiusMult.getMax();
            if (rMax < 0) {
                rMax = 0;
            }
            float[] bb = new float[]{-rMax, -rMax, -rMax, rMax, rMax, rMax};
            transform.transformBB(bb);

            float minX = Math.min(bb[0], bb[3]);
            float minY = Math.min(bb[1], bb[4]);
            float minZ = Math.min(bb[2], bb[5]);

            minPosition = new Vector3i(minX, minY, minZ);

            float maxX = Math.max(bb[0], bb[3]);
            float maxY = Math.max(bb[1], bb[4]);
            float maxZ = Math.max(bb[2], bb[5]);

            maxPosition = new Vector3i(maxX + 1, maxY + 1, maxZ + 1);

            // calculate noise levels from size of BB
            float maxSize = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ)) * 0.2F;
            noiseLevels = (maxSize <= 1) ? 0 : (int) (Math.log(maxSize) / Math.log(2) + 0.5F);

            // store transforms
            mat = transform.clone();
            if (transform.determinant() != 0) {
                invMat = transform.inverse();  // note - this alters the transform argument
            } else {
                invMat = null; // at least one axis of sphere has zero length
            }
        }

        /**
         * Get total 1/f noise value at the specified position
         */
        public float getNoise(float x, float y, float z) {
            double noise = 0;
            for (int i = 0; i < noiseLevels; i++) {
                float im = (1 << i);
                noise += (1 / im) * noiseGen.noise(x * im, y * im, z * im); // add 1/f noise
            }
            return (float) noise;
        }

        @Override
        public void generateStructure(StructureCallback callback) {
            if (invMat == null) {
                return; // sphere has zero volume and therefore cannot contain blocks
            }

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
                        callback.replaceBlock(new Vector3i(x, y, z), 1, blockProvider.getBlock((float) Math.sqrt(r2)));
                    }
                }
            }
        }
    }
}
