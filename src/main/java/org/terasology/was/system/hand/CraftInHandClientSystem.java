package org.terasology.was.system.hand;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.WindowListener;
import org.terasology.was.event.CraftInHandButton;
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

    private UICraftInHand uiWindow;

    @Override
    public void initialise() {
        guiManager.registerWindow("WoodAndStone:CraftInHand", UICraftInHand.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void craftRequested(CraftInHandButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN && uiWindow == null) {
            uiWindow = (UICraftInHand) guiManager.openWindow("WoodAndStone:CraftInHand");
            uiWindow.setCharacterEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
            uiWindow.addWindowListener(
                    new WindowListener() {
                        @Override
                        public void initialise(UIDisplayElement element) {
                        }

                        @Override
                        public void shutdown(UIDisplayElement element) {
                            uiWindow = null;
                        }
                    }
            );
            event.consume();
        }
    }
//
//    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
//    public void characterInventoryChanged()
}
