package org.terasology.multiBlock;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.In;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class MultiBlockFormingSystem extends BaseComponentSystem {
    @In
    private MultiBlockFormRecipeRegistry recipeRegistry;

    @ReceiveEvent(components = {ItemComponent.class})
    public void formMultiBlockWithItem(ActivateEvent event, EntityRef item) {
        for (MultiBlockFormItemRecipe multiBlockFormItemRecipe : recipeRegistry.getMultiBlockFormItemRecipes()) {
            if (multiBlockFormItemRecipe.isActivator(item)) {
                if (multiBlockFormItemRecipe.processActivation(event)) {
                    break;
                }
            }
        }
    }
}
