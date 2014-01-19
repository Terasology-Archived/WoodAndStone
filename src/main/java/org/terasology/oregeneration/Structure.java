package org.terasology.oregeneration;

import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface Structure {
    public float getWeightForChunkPosition(Vector3i position);

    public Block getBlockToPlaceInChunkPosition(Vector3i position);
}
