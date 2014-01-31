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
package org.terasology.bronze.ui;

import org.terasology.bronze.component.CharcoalPitComponent;
import org.terasology.bronze.event.ProduceCharcoalRequest;
import org.terasology.bronze.system.CharcoalPitUtils;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.math.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIInventoryGrid;
import org.terasology.rendering.gui.windows.UIScreenInventory;

import javax.vecmath.Vector2f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UICharcoalPit extends UIScreenInventory {
    private EntityRef charcoalPitEntity;

    private int windowWidth = 500;
    private int windowHeight = 320;

    private UIInventoryGrid inputGrid;
    private UIInventoryGrid outputGrid;
    private UIButton processButton;

    public void setCharcoalPit(final EntityRef entity) {
        this.charcoalPitEntity = entity;

        CharcoalPitComponent charcoalPit = entity.getComponent(CharcoalPitComponent.class);

        inputGrid = new UIInventoryGrid(5);
        inputGrid.linkToEntity(entity, 0, charcoalPit.inputSlotCount);
        inputGrid.setPosition(new Vector2f(0, 0));

        outputGrid = new UIInventoryGrid(5);
        outputGrid.linkToEntity(entity, charcoalPit.inputSlotCount, charcoalPit.outputSlotCount);
        inputGrid.setPosition(new Vector2f(windowWidth - 150, 0));

        processButton = new UIButton(new Vector2f(80, 38), UIButton.ButtonType.NORMAL);
        processButton.getLabel().setText("To Charcoal");
        processButton.setPosition(new Vector2f(150, 0));
        processButton.addClickListener(
                new ClickListener() {
                    @Override
                    public void click(UIDisplayElement element, int button) {
                        entity.send(new ProduceCharcoalRequest());
                    }
                });

        addDisplayElement(inputGrid);
        addDisplayElement(processButton);
        addDisplayElement(outputGrid);

        Vector2i displaySize = getDisplaySize();
        setSize(new Vector2f(windowWidth, windowHeight));
        setPosition(new Vector2f((displaySize.x - windowWidth) / 2, (displaySize.y - windowHeight) / 2));
    }

    public void update() {
        super.update();

        if (!charcoalPitEntity.exists()) {
            close();
            return;
        }

        long worldTime = CoreRegistry.get(Time.class).getGameTimeInMs();

        CharcoalPitComponent charcoalPit = charcoalPitEntity.getComponent(CharcoalPitComponent.class);
        if (charcoalPit.burnFinishWorldTime > worldTime) {
            // It's burning wood now
            inputGrid.setVisible(false);
            processButton.setVisible(false);
            outputGrid.setVisible(false);
        } else {
            // It's not burning wood
            inputGrid.setVisible(true);
            outputGrid.setVisible(true);

            SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);

            int logCount = CharcoalPitUtils.getLogCount(inventoryManager, charcoalPitEntity);

            processButton.setVisible(CharcoalPitUtils.canBurnCharcoal(inventoryManager, logCount, charcoalPitEntity));
        }
    }
}
