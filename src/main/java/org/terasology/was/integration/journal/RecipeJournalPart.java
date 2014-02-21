/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.was.integration.journal;

import org.terasology.asset.Assets;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.journal.JournalManager;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.world.block.Block;

public class RecipeJournalPart implements JournalManager.JournalEntryPart {
    private int indentAbove = 5;
    private int indentBelow = 5;
    private int ingredientSpacing = 3;
    private int resultSpacing = 30;

    private int iconSize = 64;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;
    private ItemIcon[] ingredientIcons;
    private ItemIcon resultIcon;

    public RecipeJournalPart(Block[] blockIngredients, Prefab[] itemIngredients, Block blockResult, Prefab itemResult, int resultCount) {
        if (blockIngredients.length != itemIngredients.length) {
            throw new IllegalArgumentException("Arrays have to be of the same length");
        }
        ingredientIcons = new ItemIcon[blockIngredients.length];
        for (int i = 0; i < ingredientIcons.length; i++) {
            ItemIcon itemIcon = new ItemIcon();
            if (blockIngredients[i] != null) {
                initializeForBlock(itemIcon, blockIngredients[i]);
            } else {
                initializeForItem(itemIcon, itemIngredients[i]);
            }
            ingredientIcons[i] = itemIcon;
        }
        resultIcon = new ItemIcon();
        if (blockResult != null) {
            initializeForBlock(resultIcon, blockResult);
        } else {
            initializeForItem(resultIcon, itemResult);
        }
        resultIcon.setQuantity(resultCount);
    }

    private void initializeForItem(ItemIcon itemIcon, Prefab itemIngredient) {
        ItemComponent item = itemIngredient.getComponent(ItemComponent.class);
        DisplayNameComponent displayName = itemIngredient.getComponent(DisplayNameComponent.class);
        itemIcon.setIcon(item.icon);
        if (displayName != null) {
            itemIcon.setTooltip(displayName.name);
        }
    }

    private void initializeForBlock(ItemIcon itemIcon, Block blockIngredient) {
        itemIcon.setMesh(blockIngredient.getMesh());
        itemIcon.setMeshTexture(Assets.getTexture("engine:terrain"));
        itemIcon.setTooltip(blockIngredient.getDisplayName());
    }

    @Override
    public Vector2i getPreferredSize(Canvas canvas, long date) {
        int x = 0;
        int y = 0;

        int ingredientsCount = ingredientIcons.length;
        x += ingredientsCount * iconSize + (ingredientsCount - 1) * ingredientSpacing;
        x += resultSpacing + iconSize;

        y += iconSize;

        y += indentAbove + indentBelow;
        return new Vector2i(x, y);
    }

    @Override
    public void render(Canvas canvas, Rect2i region, long date) {
        int ingredientsCount = ingredientIcons.length;
        int drawingWidth = ingredientsCount * iconSize + (ingredientsCount - 1) * ingredientSpacing + resultSpacing + iconSize;
        int x = region.minX() + horizontalAlign.getOffset(drawingWidth, region.width());
        int y = region.minY() + indentAbove;
        for (int i = 0; i < ingredientIcons.length; i++) {
            canvas.drawWidget(ingredientIcons[i], Rect2i.createFromMinAndSize(x, y, iconSize, iconSize));
            x += iconSize + ingredientSpacing;
        }
        x -= ingredientSpacing;
        x += resultSpacing;
        canvas.drawWidget(resultIcon, Rect2i.createFromMinAndSize(x, y, iconSize, iconSize));
    }
}
