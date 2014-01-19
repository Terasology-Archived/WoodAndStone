package org.terasology.oregeneration;

import org.terasology.math.Vector3i;
import org.terasology.utilities.random.FastRandom;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class StructureGenerator {
    protected FastRandom createRandomForChunk(String seed, Vector3i chunkCoordinates, int type) {
        return new FastRandom(seed.hashCode() + type + chunkCoordinates.x + chunkCoordinates.z);
    }
}
