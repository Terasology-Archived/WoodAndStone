package org.terasology.multiBlock;

import org.terasology.entitySystem.entity.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityFilter {
    public boolean accepts(EntityRef entity);
}
