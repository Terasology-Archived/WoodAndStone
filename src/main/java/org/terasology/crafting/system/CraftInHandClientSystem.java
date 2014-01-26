/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.system;

import org.terasology.crafting.event.CraftInHandButton;
import org.terasology.crafting.ui.UICraftInHand;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.WindowListener;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CraftInHandClientSystem implements ComponentSystem {
    @In
    private GUIManager guiManager;
    @In
    private SlotBasedInventoryManager inventoryManager;
    @In
    private CraftInHandRecipeRegistry recipeRegistry;

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
        if (!recipeRegistry.isCraftingInHandDisabled()) {
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
    }
}
