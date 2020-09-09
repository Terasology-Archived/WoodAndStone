// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system;

import org.terasology.crafting.event.CraftInHandButton;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.nui.input.ButtonState;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CraftInHandClientSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;
    @In
    private CraftInHandRecipeRegistry recipeRegistry;

    @ReceiveEvent
    public void craftRequested(CraftInHandButton event, EntityRef entity,
                               ClientComponent clientComponent) {
        if (!recipeRegistry.isCraftingInHandDisabled()) {
            if (event.getState() == ButtonState.DOWN) {
                nuiManager.toggleScreen("WoodAndStone:CraftInHand");
            }
        }
    }
}
