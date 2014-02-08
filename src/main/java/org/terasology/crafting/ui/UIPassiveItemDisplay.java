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
package org.terasology.crafting.ui;

import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIItemIcon;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.world.block.items.BlockItemComponent;

import javax.vecmath.Vector2f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UIPassiveItemDisplay extends UIDisplayContainer {
    private static final Vector2f DEFAULT_ICON_POSITION = new Vector2f(2f, 2f);

    private static final Vector2f ITEM_LABEL_POSITION = new Vector2f(0f, -14f);

    private UIImage background;
    private UILabel itemLabel;
    private UIItemIcon icon;

    private MouseMoveListener mouseMoveListener = new MouseMoveListener() {
        @Override
        public void leave(UIDisplayElement element) {
            itemLabel.setVisible(false);
        }

        @Override
        public void hover(UIDisplayElement element) {

        }

        @Override
        public void enter(UIDisplayElement element) {
            itemLabel.setVisible(true);
        }

        @Override
        public void move(UIDisplayElement element) {
        }
    };

    public UIPassiveItemDisplay(InventoryManager inventoryManager, EntityRef item, Integer count) {
        background = new UIImage(Assets.getTexture("engine:inventory"));
        background.setTextureSize(new Vector2f(19f, 19f));
        background.setTextureOrigin(new Vector2f(3f, 146f));
        background.setSize(getSize());
        background.setVisible(true);
        background.setFixed(true);

        itemLabel = new UILabel();
        itemLabel.setVisible(false);
        itemLabel.setPosition(ITEM_LABEL_POSITION);
        itemLabel.setText(getLabelFor(item));

        icon = new UIItemIcon();
        icon.setPosition(DEFAULT_ICON_POSITION);
        if (count != null) {
            icon.setFixedItemCount(count);
        }
        icon.setItem(item);
        icon.setVisible(true);

        addMouseMoveListener(mouseMoveListener);

        addDisplayElement(background);
        addDisplayElement(icon);
        addDisplayElement(itemLabel);
    }

    private String getLabelFor(EntityRef item) {
        DisplayNameComponent info = item.getComponent(DisplayNameComponent.class);
        if (info != null) {
            return info.name;
        }
        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        if (blockItem != null) {
            return blockItem.blockFamily.getDisplayName();
        }

        return "";
    }
}
