// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.heat.HeatUtils;
import org.terasology.heat.component.HeatProducerComponent;
import org.terasology.heat.ui.ThermometerWidget;
import org.terasology.inventory.rendering.nui.layers.ingame.InventoryGrid;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.workstation.component.WorkstationInventoryComponent;

public final class WorkstationScreenUtils {
    private WorkstationScreenUtils() {
    }

    public static void setupInventoryGrid(EntityRef workstation, InventoryGrid inventoryGrid, String type) {
        WorkstationInventoryComponent workstationInventory =
                workstation.getComponent(WorkstationInventoryComponent.class);
        WorkstationInventoryComponent.SlotAssignment assignment = workstationInventory.slotAssignments.get(type);

        inventoryGrid.setTargetEntity(workstation);
        inventoryGrid.setCellOffset(assignment.slotStart);
        inventoryGrid.setMaxCellCount(assignment.slotCount);
    }

    public static void setupTemperatureWidget(final EntityRef workstation, ThermometerWidget thermometerWidget,
                                              float minimumTemperature) {
        thermometerWidget.bindMaxTemperature(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        HeatProducerComponent producer = workstation.getComponent(HeatProducerComponent.class);
                        return producer.maximumTemperature;
                    }

                    @Override
                    public void set(Float value) {
                    }
                }
        );
        thermometerWidget.setMinTemperature(minimumTemperature);

        thermometerWidget.bindTemperature(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        return HeatUtils.calculateHeatForEntity(workstation,
                                CoreRegistry.get(BlockEntityRegistry.class));
                    }

                    @Override
                    public void set(Float value) {
                    }
                });
        thermometerWidget.bindTooltipString(
                new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        return Math.round(HeatUtils.calculateHeatForEntity(workstation,
                                CoreRegistry.get(BlockEntityRegistry.class))) + "C";
                    }
                });
    }
}
