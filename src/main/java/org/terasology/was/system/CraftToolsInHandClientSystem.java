package org.terasology.was.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.was.component.PlantFibreComponent;
import org.terasology.was.ui.UICraftInHand;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CraftToolsInHandClientSystem implements ComponentSystem {
    @In
    private GUIManager guiManager;

    @Override
    public void initialise() {
        guiManager.registerWindow("WoodAndStone:CraftInHand", UICraftInHand.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {PlantFibreComponent.class})
    public void onPlantFibreUse(ActivateEvent event, EntityRef entity) {
        guiManager.openWindow("WoodAndStone:CraftInHand");
    }
}
