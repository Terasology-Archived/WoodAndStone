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
package org.terasology.crafting.system.recipe.render;

import com.google.common.base.Function;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.world.block.items.BlockItemComponent;

public class ItemSlotIngredientRenderer implements CraftIngredientRenderer {
    private ItemIcon itemIcon;
    private Function<Integer, Integer> multiplierFunction;

    public ItemSlotIngredientRenderer() {
        itemIcon = new ItemIcon();
    }

    public void update(EntityRef entity, int slot, Function<Integer, Integer> multiplierFunction) {
        this.multiplierFunction = multiplierFunction;

        EntityRef item = InventoryUtils.getItemAt(entity, slot);
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        BlockItemComponent blockItemComp = item.getComponent(BlockItemComponent.class);
        if (itemComp != null && itemComp.renderWithIcon) {
            itemIcon.setIcon(itemComp.icon);
        } else if (blockItemComp != null) {
            itemIcon.setMesh(blockItemComp.blockFamily.getArchetypeBlock().getMesh());
            itemIcon.setMeshTexture(Assets.getTexture("engine:terrain"));
        }
        GetItemTooltip tooltipEvent;
        DisplayNameComponent displayName = item.getComponent(DisplayNameComponent.class);
        if (displayName != null) {
            tooltipEvent = new GetItemTooltip(displayName.name);
        } else {
            tooltipEvent = new GetItemTooltip();
        }
        item.send(tooltipEvent);

        itemIcon.setTooltipLines(tooltipEvent.getTooltipLines());
    }

    @Override
    public Vector2i getPreferredSize(Canvas canvas, int multiplier) {
        return new Vector2i(56, 56);
    }

    @Override
    public void render(Canvas canvas, Rect2i region, int multiplier) {
        itemIcon.setQuantity(multiplierFunction.apply(multiplier));
        canvas.drawWidget(itemIcon, region);
    }
}
