package org.terasology.multiBlock;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.ActivateEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface MultiBlockFormItemRecipe {
    public boolean isActivator(EntityRef item);

    public boolean processActivation(ActivateEvent event);
}
