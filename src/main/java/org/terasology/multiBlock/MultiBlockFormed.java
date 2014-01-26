package org.terasology.multiBlock;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class MultiBlockFormed implements Event {
    private EntityRef instigator;

    public MultiBlockFormed(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
