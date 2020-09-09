// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.ui;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.BaseInteractionScreen;
import org.terasology.inventory.logic.InventoryUtils;
import org.terasology.inventory.rendering.nui.layers.ingame.InventoryGrid;
import org.terasology.mill.component.MillProgressComponent;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.ActivateEventListener;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILoadBar;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.event.WorkstationProcessRequest;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class QuernWindow extends BaseInteractionScreen {
    private InventoryGrid input;
    private InventoryGrid output;
    private UIButton millButton;
    private UILoadBar millingProgress;

    @Override
    public void initialise() {
        input = find("input", InventoryGrid.class);
        output = find("output", InventoryGrid.class);

        millButton = find("millButton", UIButton.class);
        millingProgress = find("millingProgress", UILoadBar.class);

        InventoryGrid player = find("player", InventoryGrid.class);
        player.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        player.setCellOffset(10);
        player.setMaxCellCount(30);
    }

    @Override
    protected void initializeWithInteractionTarget(final EntityRef workstation) {
        WorkstationScreenUtils.setupInventoryGrid(workstation, input, "INPUT");
        WorkstationScreenUtils.setupInventoryGrid(workstation, output, "OUTPUT");

        millButton.bindVisible(
                new Binding<Boolean>() {
                    @Override
                    public Boolean get() {
                        return (!workstation.hasComponent(WorkstationProcessingComponent.class)
                                && (workstation.hasComponent(MillProgressComponent.class) || InventoryUtils.getItemAt(workstation, 0).exists()));
                    }

                    @Override
                    public void set(Boolean value) {
                    }
                });
        millButton.subscribe(
                new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget widget) {
                        EntityRef instigator = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();

                        instigator.send(new WorkstationProcessRequest(workstation, "Prefab:WoodAndStone" +
                                ":MillingProcess"));
                    }
                });

        millingProgress.bindValue(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        MillProgressComponent millProgress = workstation.getComponent(MillProgressComponent.class);
                        if (millProgress == null) {
                            return 0f;
                        }
                        WorkstationProcessingComponent processing =
                                workstation.getComponent(WorkstationProcessingComponent.class);
                        if (processing == null) {
                            return millProgress.processedStep * 0.25f;
                        }
                        WorkstationProcessingComponent.ProcessDef processDef = processing.processes.get("WoodAndStone" +
                                ":Milling");

                        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

                        return millProgress.processedStep * 0.25f + 0.25f * (gameTime - processDef.processingStartTime)
                                / (processDef.processingFinishTime - processDef.processingStartTime);
                    }

                    @Override
                    public void set(Float value) {
                    }
                });
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
