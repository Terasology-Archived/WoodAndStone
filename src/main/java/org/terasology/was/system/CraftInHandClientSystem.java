package org.terasology.was.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.network.ClientComponent;
import org.terasology.was.component.CraftInHandRecipeComponent;
import org.terasology.was.event.CraftInHandButton;
import org.terasology.was.event.UserClosedCraftInHandUI;
import org.terasology.was.event.UserCraftInHandRequest;
import org.terasology.was.ui.UICraftInHand;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CraftInHandClientSystem implements ComponentSystem {
    @In
    private GUIManager guiManager;
    @In
    private SlotBasedInventoryManager inventoryManager;

    private boolean windowOpened = false;

    @Override
    public void initialise() {
        guiManager.registerWindow("WoodAndStone:CraftInHand", UICraftInHand.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class, ClientComponent.class})
    public void craftRequested(CraftInHandButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN && !windowOpened) {
            UICraftInHand uiWindow = (UICraftInHand) guiManager.openWindow("WoodAndStone:CraftInHand");
            uiWindow.setCharacterEntity(entity);
            windowOpened = true;
            event.consume();
        }
    }

    @ReceiveEvent
    public void onCraftInHandUIClose(UserClosedCraftInHandUI event, EntityRef character) {
        windowOpened = false;
        EntityRef entity1 = event.getEntity1();
        EntityRef entity2 = event.getEntity2();
        EntityRef entity3 = event.getEntity3();

        CraftInHandRecipeComponent entity1Component = entity1.getComponent(CraftInHandRecipeComponent.class);
        CraftInHandRecipeComponent entity2Component = entity2.getComponent(CraftInHandRecipeComponent.class);
        CraftInHandRecipeComponent entity3Component = entity3.getComponent(CraftInHandRecipeComponent.class);

        EntityRef item1 = EntityRef.NULL;
        EntityRef item2 = EntityRef.NULL;
        EntityRef item3 = EntityRef.NULL;

        if (entity1Component != null)
            item1 = findItemInInventoryWithComponentType(character, entity1Component.componentType);
        if (entity2Component != null)
            item2 = findItemInInventoryWithComponentType(character, entity2Component.componentType);
        if (entity3Component != null)
            item3 = findItemInInventoryWithComponentType(character, entity3Component.componentType);

        character.send(new UserCraftInHandRequest(item1, item2, item3));

        entity1.destroy();
        entity2.destroy();
        entity3.destroy();
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
