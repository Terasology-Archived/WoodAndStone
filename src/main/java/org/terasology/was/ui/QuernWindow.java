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
package org.terasology.was.ui;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.mill.component.MillProgressComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILoadBar;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;
import org.terasology.workstation.ui.WorkstationUI;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class QuernWindow extends CoreScreenLayer implements WorkstationUI {
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
    public void initializeWorkstation(final EntityRef workstation) {
        input.setTargetEntity(workstation);
        input.setCellOffset(0);
        input.setMaxCellCount(1);

        output.setTargetEntity(workstation);
        output.setCellOffset(1);
        output.setMaxCellCount(1);

        millButton.bindVisible(
                new Binding<Boolean>() {
                    @Override
                    public Boolean get() {
                        WorkstationProcessingComponent processing = workstation.getComponent(WorkstationProcessingComponent.class);
                        return (processing == null && InventoryUtils.getItemAt(workstation, 0).exists());
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

                        Collection<WorkstationProcess> workstationProcesses = CoreRegistry.get(WorkstationRegistry.class).getWorkstationProcesses(Collections.singleton("WoodAndStone:Milling"));
                        for (WorkstationProcess workstationProcess : workstationProcesses) {
                            try {
                                Set<String> possibleResultIds = new HashSet<>();
                                for (ProcessPart processPart : workstationProcess.getProcessParts()) {
                                    Set<String> resultIds = processPart.validate(instigator, workstation, null);
                                    if (resultIds != null) {
                                        possibleResultIds.addAll(resultIds);
                                    }
                                }

                                if (possibleResultIds.size() <= 1) {
                                    String resultId = possibleResultIds.size() == 0 ? null : possibleResultIds.iterator().next();
                                    workstation.send(new WorkstationProcessRequest(instigator, workstationProcess.getId(), resultId, null));
                                    return;
                                }
                            } catch (InvalidProcessException exp) {
                                // continue
                            }
                        }

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
                        WorkstationProcessingComponent processing = workstation.getComponent(WorkstationProcessingComponent.class);
                        if (processing == null) {
                            return millProgress.processedStep * 0.25f;
                        }
                        WorkstationProcessingComponent.ProcessDef processDef = processing.processes.get("WoodAndStone:Milling");

                        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

                        return millProgress.processedStep * 0.25f + 0.25f * (gameTime - processDef.processingStartTime) / (processDef.processingFinishTime - processDef.processingStartTime);
                    }

                    @Override
                    public void set(Float value) {
                    }
                });
    }
}
