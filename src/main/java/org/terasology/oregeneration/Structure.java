package org.terasology.oregeneration;

import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface Structure {
    void generateStructure(StructureCallback callback);

    public interface StructureCallback {
        void replaceBlock(Vector3i position, float force, Block block);

        boolean canReplace(int x, int y, int z);
    }
}
