package org.terasology.oregeneration;

import org.terasology.math.Vector3i;
import org.terasology.oregeneration.util.PDist;
import org.terasology.oregeneration.util.Transform;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class VeinStructureGenerator extends AbstractBlockStructureGenerator {

    private static final int generatorSalt = 2014;

    private PDist motherLodeFrequency;
    private PDist motherLodeRadius;
    private PDist motherLodeYLevel;

    private PDist branchFrequency;
    private PDist branchInclination;
    private PDist branchLength;
    private PDist branchHeightLimit;

    private PDist segmentLength;
    private PDist segmentAngle;
    private PDist segmentRadius;

    private PDist blockDensity;
    private PDist blockRadiusMultiplier;

    private Block motherLodeBlock;
    private Block block;

    public VeinStructureGenerator(Block motherLodeBlock, Block block,
                                  PDist motherLodeFrequency, PDist motherLodeRadius, PDist motherLodeYLevel,
                                  PDist branchFrequency, PDist branchInclination, PDist branchLength, PDist branchHeightLimit,
                                  PDist segmentLength, PDist segmentAngle, PDist segmentRadius,
                                  PDist blockDensity, PDist blockRadiusMultiplier) {
        this.motherLodeBlock = motherLodeBlock;
        this.block = block;
        this.motherLodeFrequency = motherLodeFrequency;
        this.motherLodeRadius = motherLodeRadius;
        this.motherLodeYLevel = motherLodeYLevel;
        this.branchFrequency = branchFrequency;
        this.branchInclination = branchInclination;
        this.branchLength = branchLength;
        this.branchHeightLimit = branchHeightLimit;
        this.segmentLength = segmentLength;
        this.segmentAngle = segmentAngle;
        this.segmentRadius = segmentRadius;
        this.blockDensity = blockDensity;
        this.blockRadiusMultiplier = blockRadiusMultiplier;
    }

    @Override
    public List<Structure> generateStructures(Chunk chunk, String seed, WorldBiomeProvider worldBiomeProvider) {
        Vector3i chunkPosition = chunk.getPos();
        Vector3i chunkSize = new Vector3i(chunk.getChunkSizeX(), chunk.getChunkSizeY(), chunk.getChunkSizeZ());

        List<Structure> result = new LinkedList<>();
        int chunksRangeToEvaluate = (int) Math.ceil(motherLodeRadius.getMax() + branchLength.getMax());
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

        float clustersInChunk = motherLodeFrequency.getValue(random);
        int clustersToGenerateInChunk = (int) clustersInChunk;

        // Check if we "hit" any leftover
        if (random.nextFloat() < clustersInChunk - clustersToGenerateInChunk) {
            clustersToGenerateInChunk++;
        }

        for (int i = 0; i < clustersToGenerateInChunk; i++) {
            // Location of cluster "center"
            float mlX = random.nextFloat() * chunkSize.x + xShift;
            float mlY = motherLodeYLevel.getValue(random);
            float mlZ = random.nextFloat() * chunkSize.z + zShift;

            // motherlode transformation matrix
            Transform mlMat = new Transform();
            mlMat.translate(mlX, mlY, mlZ); // center translation
            mlMat.rotateZ(random.nextFloat() * 6.28319F); // phi rotation
            mlMat.rotateY(random.nextFloat() * 6.28319F); // theta rotation
            mlMat.scale(motherLodeRadius.getValue(random), motherLodeRadius.getValue(random), motherLodeRadius.getValue(random)); // scale axes

            // build transformed bounding box from the local BB for a unit sphere
            float rMax = blockRadiusMultiplier.getMax();
            if (rMax < 0) {
                rMax = 0;
            }
            float[] bb = new float[]{-rMax, -rMax, -rMax, rMax, rMax, rMax};
            mlMat.transformBB(bb);

            float minX = Math.min(bb[0], bb[3]);
            float minY = Math.min(bb[1], bb[4]);
            float minZ = Math.min(bb[2], bb[5]);

            Vector3i minPosition = new Vector3i(minX, minY, minZ);

            float maxX = Math.max(bb[0], bb[3]);
            float maxY = Math.max(bb[1], bb[4]);
            float maxZ = Math.max(bb[2], bb[5]);

            Vector3i maxPosition = new Vector3i(maxX + 1, maxY + 1, maxZ + 1);

            // store transforms
            if (mlMat.determinant() != 0) {
                Transform invMat = mlMat.inverse();

                // create motherlode component
                result.add(new MotherLodeStructure(seedForChunk, chunkSize, chunkPosition, minPosition, maxPosition, invMat));
            }

            // create random number of branches
            for (int br = branchFrequency.getIntValue(random); br > 0; br--) {
                Random brRandom = new FastRandom(random.nextLong());
                // initialize branch transform
                Transform segMat = new Transform();
                segMat.translate(mlX, mlY, mlZ);  // motherlode translation
                segMat.rotateY(brRandom.nextFloat() * 6.28319F); // random rotation about vertical
                segMat.rotateX(-branchInclination.getValue(brRandom)); // angle from horizontal

                // calculate height limits
                float maxHeight = mlY + branchHeightLimit.getValue(brRandom);
                float minHeight = mlY - branchHeightLimit.getValue(brRandom);

                // create branch
                generateBranch(result, branchLength.getValue(brRandom), maxHeight, minHeight, segMat, brRandom, seedForChunk, chunkPosition);
            }

        }
    }

    public void generateBranch(List<Structure> result, float length, float maxHeight, float minHeight, Transform mat, Random random, long seed, Vector3i chunkPosition) {
        float[] pos = new float[3];
        // create segments until max branch length is reached
        while (length > 0) {
            // determine segment length & radius
            float segLen = segmentLength.getValue(random);
            if (segLen > length) {
                segLen = length;
            }
            length -= segLen;

            float segRad = segmentRadius.getValue(random);

            // translate to center point
            mat.translate(0, 0, segLen);

            // create segment component
            Transform segMat = mat.clone().scale(segRad, segRad, segLen);

            result.add(new TubeStructure(segMat, seed, chunkPosition));

            // translate to end point
            mat.translate(0, 0, segLen);
            // calculate end point
            pos[0] = 0;
            pos[1] = 0;
            pos[2] = 0;
            mat.transformVector(pos);

            // validate coordinates for next segment
            if (pos[1] > maxHeight || pos[1] < minHeight) {
                return;    // branch extends outside of vertical range
            }

            // rotate relative to arbitrary axis in XY plane
            float axisTheta = random.nextFloat() * 6.28319F;
            mat.rotate(segmentAngle.getValue(random), (float) Math.cos(axisTheta), (float) Math.sin(axisTheta), 0);
        }
    }


    private long getLocalizedSeed(long seed, Vector3i position) {
        return seed + 97231 * (31 * (31 * position.x + position.y) + position.z);
    }

    private class TubeStructure implements Structure {
        // center and forward control points
        protected float[] mid;
        protected float[] end;
        // radius
        protected final float rad;
        // interpolation context & persistent transform object
        protected final InterpolationContext context;
        protected final Transform mat;
        private long seed;
        private Vector3i chunkPosition;

        public TubeStructure(Transform transform, long seed, Vector3i chunkPosition) {
            this.seed = seed;
            this.chunkPosition = chunkPosition;
            // calculate midpoint & endpoint
            mid = new float[]{0, 0, 0};
            transform.transformVector(mid);
            end = new float[]{0, 0, 1};
            transform.transformVector(end);
            // calculate radius (along x axis, we assume it is the same along the y)
            float[] xunit = new float[]{1, 0, 0, 0};
            transform.transformVector(xunit);
            rad = (float) Math.sqrt(xunit[0] * xunit[0] + xunit[1] * xunit[1] + xunit[2] * xunit[2]);

            // construct a persistent context for interpolation loops
            context = new InterpolationContext();
            mat = transform.identity();
        }

        @Override
        public void generateStructure(StructureCallback callback) {
            FastRandom random = new FastRandom(getLocalizedSeed(seed, chunkPosition));

            // get min & max radii in local coordinates
            float maxR = blockRadiusMultiplier.getMax();
            if (maxR < 0) {
                maxR = 0;
            }
            float maxR2 = maxR * maxR;
            float minR = blockRadiusMultiplier.getMin();
            if (minR < 0) {
                minR = 0;
            }
            float minR2 = minR * minR;

            // interpolate over segment
            float[] pos = new float[3];
            int innerStep = 1;
            context.init(0, true);
            do {
                // determine step size and count
                innerStep = (int) context.radius / 4 + 1;
                if (context.radius <= 0) {
                    continue; // zero radius
                }
                float step = 0.7F * innerStep / context.radius;
                int stepCount = (int) (maxR / step) + 1;
                boolean oneBlockThreshold = (context.radius * maxR < 0.25F); // radius is too small even for a single block
                // build transformation
                mat.identity();
                mat.translate(context.pos[0], context.pos[1], context.pos[2]);
                mat.rotateZInto(context.der[0], context.der[1], context.der[2]);
                mat.scale(context.radius, context.radius, innerStep);
                // iterate through blocks in the local XY plane
                for (int x = -stepCount; x < stepCount; x++) {
                    for (int y = -stepCount; y < stepCount; y++) {
                        pos[0] = x * step;
                        pos[1] = y * step;
                        pos[2] = 0;
                        // check radius
                        float r2 = pos[0] * pos[0] + pos[1] * pos[1];
                        if (r2 > maxR2) {
                            continue; // block is outside maximum possible radius
                        }
                        if (r2 > minR2) // block is near tube surface
                        {
                            float rMax = blockRadiusMultiplier.getValue(random);
                            if (r2 > rMax * rMax) {
                                continue; // block is outside maximum radius
                            }
                        }
                        if (oneBlockThreshold && context.radius * maxR * 4 < random.nextFloat()) {
                            continue; // blocks must pass random check for very thin tubes
                        }
                        // transform into world coordinates
                        mat.transformVector(pos);
                        int baseX = (int) (Math.floor(pos[0]) - innerStep / 2);
                        int baseY = (int) (Math.floor(pos[1]) - innerStep / 2);
                        int baseZ = (int) (Math.floor(pos[2]) - innerStep / 2);
                        // iterate over inner group
                        for (int blockX = baseX; blockX < innerStep + baseX; blockX++) {
                            for (int blockY = baseY; blockY < innerStep + baseY; blockY++) {
                                for (int blockZ = baseZ; blockZ < innerStep + baseZ; blockZ++) {
                                    if (blockDensity.getIntValue(random) < 1) {
                                        continue; // density check failed
                                    }
                                    if (callback.canReplace(blockX, blockY, blockZ)) {
                                        callback.replaceBlock(new Vector3i(blockX, blockY, blockZ), 1, block);
                                    }
                                }
                            }
                        }
                    }
                }
                // next interpolation step
            } while (context.advance(0.7F * innerStep));

        }

        /**
         * Parametric interpolation of tube center line.
         * Quadratic bezier curves are used between neighboring segments.
         * Without a neighbor, the tube follows the straight segment line.
         *
         * @param pos set to position vector as {x,y,z}
         * @param t   interpolating parameter between [-1,1]
         */
        public void interpolatePosition(float[] pos, float t) {
            float nt = 1 - 2 * t;
            pos[0] = nt * mid[0] + 2 * t * end[0];
            pos[1] = nt * mid[1] + 2 * t * end[1];
            pos[2] = nt * mid[2] + 2 * t * end[2];
        }

        /**
         * Parametric interpolation of position derivative.
         * Without a neighbor, the tube follows the straight segment line.
         *
         * @param der set to derivative vector as {dx,dy,dz}
         * @param t   interpolating parameter between [-1,1]
         */
        public void interpolateDerivative(float[] der, float t) {
            der[0] = 2 * (end[0] - mid[0]);
            der[1] = 2 * (end[1] - mid[1]);
            der[2] = 2 * (end[2] - mid[2]);
        }

        /**
         * Parametric interpolation of segment radius.
         * Segment radius varies smoothly between neighboring segments.
         * Without a neighbor, the forward half terminates in a half-ellipsoid
         * and the backward half terminates in a cylinder of constant radius
         *
         * @param t interpolating parameter between [-1,1]
         */
        public float interpolateRadius(float t) {
            if (t <= 0 && t > -1) {
                return rad; // no backward neighbor - constant radius
            } else if (t > 0 && t < 1) {
                return (float) (rad * Math.sqrt(1 - 4 * t * t)); // no forward neighbor - approach zero as parabola
            } else {
                return 0;
            }
        }

        /**
         * Context information for discrete interpolation over the segment
         */
        private class InterpolationContext {
            public float[] pos;        // position
            public float[] der;        // normalized derivative vector
            public float derLen;    // norm of derivative
            public float radius;    // radius of tube
            public float err;        // estimated max distance^2 to corresponding points from previous step
            public float t;            // interpolation parameter
            public float dt;        // parameter step size
            public boolean calcDer;    // whether or not to calculate the derivative vector and length at each step

            /**
             * Construct a blank context object
             */
            public InterpolationContext() {
                pos = new float[3];
                der = new float[3];
                t = 10; // interpolation is not in progress
                dt = 1 / 20F; // guess at step size
            }

            /**
             * Called before an interpolation loop to initialize all state info
             *
             * @param stepSize           Initial step size to use.  Pass 0 to keep size from last interpolation.
             * @param calculateDirection Whether or not to compute normalized derivative at each step.
             */
            public void init(float stepSize, boolean calculateDirection) {
                // interpolate all the way to the center of previous segment unless it reciprocates
                t = -0.5F;
                if (stepSize > 0) {
                    dt = stepSize;
                }
                // calculate initial position
                interpolatePosition(pos, t);
                // calculate initial radius
                radius = interpolateRadius(t);
                // calculate and normalize initial derivative
                calcDer = calculateDirection;
                if (calcDer) {
                    interpolateDerivative(der, t);
                    derLen = (float) Math.sqrt(der[0] * der[0] + der[1] * der[1] + der[2] * der[2]);
                    der[0] /= derLen;
                    der[1] /= derLen;
                    der[2] /= derLen;
                } else {
                    derLen = 0;
                    der[0] = der[1] = der[2] = 0;
                }
                // set initial error to zero
                err = 0;
            }

            /**
             * Called to advance a step in the interpolation
             *
             * @return Returns false when the interpolation has reached the end of the segment
             */
            public boolean advance(float tolerance) {
                // store current state
                float pX = pos[0];
                float pY = pos[1];
                float pZ = pos[2];
                float dX = der[0];
                float dY = der[1];
                float dZ = der[2];
                float r = radius;
                // attempt to advance
                while (true) {
                    // advance parameter
                    float nt = t + dt;
                    // calculate new position and displacement^2
                    interpolatePosition(pos, nt);
                    float deltaX = pX - pos[0];
                    float deltaY = pY - pos[1];
                    float deltaZ = pZ - pos[2];
                    float d2 = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                    err = d2; // displacement ^2 due to translation along curve
                    // calculate new radius
                    radius = interpolateRadius(nt);
                    float avg2R = r + radius;
                    // calculate new derivative and sin^2 of angle between old & new derivatives
                    if (calcDer) {
                        interpolateDerivative(der, nt);
                        derLen = (float) Math.sqrt(der[0] * der[0] + der[1] * der[1] + der[2] * der[2]);
                        der[0] /= derLen;
                        der[1] /= derLen;
                        der[2] /= derLen;
                        deltaX = -dZ * der[1] + dY * der[2];
                        deltaY = dZ * der[0] - dX * der[2];
                        deltaZ = -dY * der[0] + dX * der[1];
                        float sin2 = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                        err += avg2R * avg2R * sin2; // ~ max displacement^2 due to relative rotation
                    }
                    // check error
                    float maxErr = tolerance * tolerance;
                    if (err > maxErr) {
                        dt *= 0.6;  // reduce step size -> reduce error
                    } else if (err < maxErr / 5) {
                        dt *= 1.8;    // increase step size -> fewer steps
                    } else {
                        break; // error was acceptable
                    }
                    // prevent infinite loops
                    if (dt < Math.ulp(t) * 2) {
                        throw new RuntimeException("CustomOreGen: Detected a possible infinite loop during bezier interpolation.  Please report this error.");
                    }
                }
                // advancement succeeded - advance actual parameter
                t += dt;
                return (t < 0.5F);
            }
        }
    }

    private class MotherLodeStructure implements Structure {
        private Vector3i chunkSize;
        private Vector3i minPosition;
        private Vector3i maxPosition;
        private Transform invMat;
        private long seed;
        private Vector3i chunkPosition;

        private MotherLodeStructure(long seed, Vector3i chunkSize, Vector3i chunkPosition, Vector3i minPosition, Vector3i maxPosition, Transform invMat) {
            this.seed = seed;
            this.chunkSize = chunkSize;
            this.chunkPosition = chunkPosition;
            this.minPosition = minPosition;
            this.maxPosition = maxPosition;
            this.invMat = invMat;
        }

        @Override
        public void generateStructure(StructureCallback callback) {
            FastRandom random = new FastRandom(getLocalizedSeed(seed, chunkPosition));

            // get min & max radii in local coordinates
            float maxR2 = blockRadiusMultiplier.getMax();
            if (maxR2 < 0) {
                maxR2 = 0;
            }
            maxR2 *= maxR2;
            float minR2 = blockRadiusMultiplier.getMin();
            if (minR2 < 0) {
                minR2 = 0;
            }
            minR2 *= minR2;
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
                        if (r2 > maxR2) {
                            continue; // block is outside maximum possible radius
                        }
                        if (r2 > minR2) // block is near ellipsoid surface
                        {
                            float rMax = blockRadiusMultiplier.getValue(random);
                            if (r2 > rMax * rMax) {
                                continue; // block is outside maximum radius
                            }
                        }
                        // place block
                        if (blockDensity.getIntValue(random) < 1) {
                            continue; // density check failed
                        }
                        if (callback.canReplace(x, y, z)) {
                            callback.replaceBlock(new Vector3i(x, y, z), 1, motherLodeBlock);
                        }
                    }
                }
            }
        }
    }
}
