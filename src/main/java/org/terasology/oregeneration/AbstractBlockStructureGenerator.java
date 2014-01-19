package org.terasology.oregeneration;

import org.terasology.math.Vector3i;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class AbstractBlockStructureGenerator implements BlockStructureGenerator {
    public long getSeedForChunk(String seed, Vector3i position, int salt) {
        return seed.hashCode() + salt * (31 * (31 * position.x + position.y) + position.z);
    }
}
