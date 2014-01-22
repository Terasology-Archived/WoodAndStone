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

import org.terasology.anotherWorld.util.PDist;
import org.terasology.math.Vector3i;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;

import java.util.List;

public class ClusterStructureDefinition extends AbstractMultiChunkStructureDefinition {
    private Block block;

    private PDist pocketYLevel;
    private PDist clusterRichness;

    public ClusterStructureDefinition(PDist frequency, Block block, PDist clusterRichness, PDist pocketYLevel) {
        super(frequency);
        this.block = block;
        this.clusterRichness = clusterRichness;
        this.pocketYLevel = pocketYLevel;
    }

    @Override
    protected int getGeneratorSalt() {
        return 979237;
    }

    @Override
    protected float getMaxRange() {
        return clusterRichness.getMax();
    }

    @Override
    protected void generateStructuresForChunk(List<Structure> result, Random random, Vector3i chunkSize, int xShift, int zShift) {
        // cluster X,Y,Z coordinates within chunk
        float clX = random.nextFloat() * chunkSize.x + xShift;
        float clY = pocketYLevel.getValue(random);
        float clZ = random.nextFloat() * chunkSize.z + zShift;

        result.add(new ClusterStructure(clX, clY, clZ, random, chunkSize));
    }

    private class ClusterStructure implements Structure {
        /* Cluster geometry parameters */
        protected final int size;        // scale
        protected final float[] ptA;    // segment start
        protected final float[] ptB;    // segment end
        protected final float[] rad;    // radius at each step along segment
        private Vector3i chunkSize;

        public ClusterStructure(float x, float y, float z, Random random, Vector3i chunkSize) {
            this.chunkSize = chunkSize;
            // choose segment length and horizontal angle from +Z axis
            size = clusterRichness.getIntValue(random);
            double horizAngle = random.nextFloat() * Math.PI;
            ptA = new float[3];
            ptB = new float[3];

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
            rad = new float[size + 1];
            for (int s = 0; s < rad.length; s++) {
                // radius for this step
                float ns = s / (float) (rad.length - 1);
                float baseRadius = (float) random.nextDouble() * size / 32F;
                rad[s] = ((float) Math.sin(ns * Math.PI) + 1) * baseRadius + 0.5F;
            }
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
