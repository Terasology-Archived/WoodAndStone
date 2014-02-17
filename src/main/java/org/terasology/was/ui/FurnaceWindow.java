package org.terasology.was.ui;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.was.heat.HeatProcessedComponent;
import org.terasology.was.heat.HeatProducerComponent;
import org.terasology.was.heat.HeatUtils;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.inventory.WorkstationInventoryUtils;
import org.terasology.workstation.ui.WorkstationUI;
import org.terasology.world.BlockEntityRegistry;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class FurnaceWindow extends CoreScreenLayer implements WorkstationUI {
    private InventoryGrid input;
    private InventoryGrid fuel;
    private InventoryGrid output;
    private VerticalTextureProgressWidget heat;
    private VerticalTextureProgressWidget burn;

    @Override
    public void initialise() {
        input = find("input", InventoryGrid.class);
        fuel = find("fuel", InventoryGrid.class);
        output = find("output", InventoryGrid.class);

        InventoryGrid player = find("player", InventoryGrid.class);
        player.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        player.setCellOffset(10);
        player.setMaxCellCount(30);

        heat = find("heat", VerticalTextureProgressWidget.class);
        heat.setMinY(130);
        heat.setMaxY(10);

        burn = find("burn", VerticalTextureProgressWidget.class);
        burn.setMinY(76);
        burn.setMaxY(4);
    }

    @Override
    public void initializeWorkstation(final EntityRef workstation) {
        input.setTargetEntity(workstation);
        input.setCellOffset(0);
        input.setMaxCellCount(1);

        fuel.setTargetEntity(workstation);
        fuel.setCellOffset(1);
        fuel.setMaxCellCount(1);

        output.setTargetEntity(workstation);
        output.setCellOffset(2);
        output.setMaxCellCount(1);

        heat.bindValue(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        return HeatUtils.calculateHeatForEntity(workstation, CoreRegistry.get(BlockEntityRegistry.class)) / 1000f;
                    }

                    @Override
                    public void set(Float value) {
                    }
                });
        heat.bindMark(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        WorkstationInventoryComponent workstationInventory = workstation.getComponent(WorkstationInventoryComponent.class);
                        if (workstationInventory != null) {
                            for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
                                HeatProcessedComponent heatProcessed = InventoryUtils.getItemAt(workstation, slot).getComponent(HeatProcessedComponent.class);
                                if (heatProcessed != null) {
                                    return heatProcessed.heatRequired / 1000f;
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    public void set(Float value) {
                    }
                });

        burn.bindValue(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        HeatProducerComponent heatProducer = workstation.getComponent(HeatProducerComponent.class);
                        List<HeatProducerComponent.FuelSourceConsume> consumedFuel = heatProducer.fuelConsumed;
                        if (consumedFuel.size() == 0) {
                            return 0f;
                        }
                        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

                        HeatProducerComponent.FuelSourceConsume lastConsumed = consumedFuel.get(consumedFuel.size() - 1);
                        if (gameTime > lastConsumed.startTime + lastConsumed.burnLength) {
                            return 0f;
                        }
                        return 1f * (gameTime - lastConsumed.startTime) / lastConsumed.burnLength;
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
