package org.terasology.was.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.was.component.CraftInHandRecipeComponent;
import org.terasology.was.component.PlantFibreComponent;
import org.terasology.was.event.UserClosedCraftInHandUI;
import org.terasology.was.event.UserCraftInHandRequest;
import org.terasology.was.ui.UICraftInHand;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CraftToolsInHandClientSystem implements ComponentSystem {
    @In
    private GUIManager guiManager;
    @In
    private SlotBasedInventoryManager inventoryManager;

    @Override
    public void initialise() {
        guiManager.registerWindow("WoodAndStone:CraftInHand", UICraftInHand.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {PlantFibreComponent.class})
    public void onPlantFibreUse(ActivateEvent event, EntityRef entity) {
        UICraftInHand uiWindow = (UICraftInHand) guiManager.openWindow("WoodAndStone:CraftInHand");
        uiWindow.setCharacterEntity(event.getInstigator());
    }

    @ReceiveEvent
    public void onCraftInHandUIClose(UserClosedCraftInHandUI event, EntityRef character) {
        EntityRef handleEntity = event.getHandleEntity();
        EntityRef toolHeadEntity = event.getToolHeadEntity();

        CraftInHandRecipeComponent handle = handleEntity.getComponent(CraftInHandRecipeComponent.class);
        CraftInHandRecipeComponent toolHead = toolHeadEntity.getComponent(CraftInHandRecipeComponent.class);

        if (handle != null && toolHead != null) {
            EntityRef handleItem = findItemInInventoryWithComponentType(character, handle.componentType);
            EntityRef toolHeadItem = findItemInInventoryWithComponentType(character, toolHead.componentType);

            if (handleItem != null && toolHeadItem != null) {
                character.send(new UserCraftInHandRequest(handleItem, toolHeadItem));
            }
        }

        handleEntity.destroy();
        toolHeadEntity.destroy();
    }

    private EntityRef findItemInInventoryWithComponentType(EntityRef character, String componentType) {
        int numSlots = inventoryManager.getNumSlots(character);
        for (int i = 0; i < numSlots; i++) {
            EntityRef item = inventoryManager.getItemInSlot(character, i);
            CraftInHandRecipeComponent slotComponent = item.getComponent(CraftInHandRecipeComponent.class);
            if (slotComponent != null && slotComponent.componentType.equals(componentType))
                return item;
        }
        return null;
    }
}
