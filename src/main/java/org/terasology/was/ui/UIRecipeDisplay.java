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
package org.terasology.was.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.was.event.UserCraftInHandRequest;
import org.terasology.was.system.recipe.CraftProcessDisplay;

import javax.vecmath.Vector2f;
import java.util.Map;

public class UIRecipeDisplay extends UIDisplayContainer {
    private EntityRef character;
    private String recipeId;
    private String resultId;
    private EntityRef result;

    public UIRecipeDisplay(String recipeId, String resultId, SlotBasedInventoryManager inventoryManager, EntityRef character, CraftProcessDisplay craftingRecipe) {
        this.recipeId = recipeId;
        this.resultId = resultId;
        this.character = character;
        int itemIndex = 0;
        int iconSize = 38;
        for (Map.Entry<Integer, Integer> craftingComponents : craftingRecipe.getComponentSlotAndCount().entrySet()) {
            UIPassiveItemDisplay element = new UIPassiveItemDisplay(inventoryManager, inventoryManager.getItemInSlot(character, craftingComponents.getKey()), craftingComponents.getValue());
            element.setSize(new Vector2f(iconSize, iconSize));
            element.setPosition(new Vector2f(itemIndex * iconSize, iconSize));
            addDisplayElement(element);
            itemIndex++;
        }
        result = craftingRecipe.createResultItemEntityForDisplayOne();
        UIPassiveItemDisplay resultElement = new UIPassiveItemDisplay(inventoryManager, result, 1);
        resultElement.setSize(new Vector2f(iconSize, iconSize));
        resultElement.setPosition(new Vector2f(4 * iconSize, iconSize));
        addDisplayElement(resultElement);

        UIButton button = new UIButton(new Vector2f(80, iconSize), UIButton.ButtonType.NORMAL);
        button.getLabel().setText("Craft 1");
        button.setPosition(new Vector2f(6 * iconSize, iconSize));
        addDisplayElement(button);
        button.addClickListener(
                new ClickListener() {
                    @Override
                    public void click(UIDisplayElement element, int button) {
                        produceOne();
                    }
                }
        );
        layout();
    }

    public void dispose() {
        result.destroy();
    }

    private void produceOne() {
        character.send(new UserCraftInHandRequest(recipeId, resultId));
    }
}
