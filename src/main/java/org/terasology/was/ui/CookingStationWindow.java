package org.terasology.was.ui;

import org.terasology.crafting.ui.workstation.StationAvailableRecipesWidget;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.fluid.component.FluidComponent;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.system.FluidRegistry;
import org.terasology.heat.HeatProducerComponent;
import org.terasology.heat.HeatUtils;
import org.terasology.heat.ui.TermometerWidget;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.ui.WorkstationUI;
import org.terasology.world.BlockEntityRegistry;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CookingStationWindow extends CoreScreenLayer implements WorkstationUI {

    private InventoryGrid fluidContainerInput;
    private InventoryGrid fluidContainerOutput;
    private FluidContainerWidget fluidContainer;
    private InventoryGrid ingredientsInventory;
    private InventoryGrid toolsInventory;
    private TermometerWidget temperature;
    private VerticalTextureProgressWidget burn;
    private InventoryGrid fuelInput;
    private StationAvailableRecipesWidget availableRecipes;
    private InventoryGrid resultInventory;

    @Override
    public void initialise() {
        ingredientsInventory = find("ingredientsInventory", InventoryGrid.class);
        toolsInventory = find("toolsInventory", InventoryGrid.class);

        fluidContainerInput = find("fluidContainerInput", InventoryGrid.class);
        fluidContainer = find("fluidContainer", FluidContainerWidget.class);
        fluidContainerOutput = find("fluidContainerOutput", InventoryGrid.class);

        temperature = find("temperature", TermometerWidget.class);

        burn = find("burn", VerticalTextureProgressWidget.class);
        burn.setMinY(76);
        burn.setMaxY(4);

        fuelInput = find("fuelInput", InventoryGrid.class);

        availableRecipes = find("availableRecipes", StationAvailableRecipesWidget.class);

        resultInventory = find("resultInventory", InventoryGrid.class);

        InventoryGrid playerInventory = find("playerInventory", InventoryGrid.class);

        playerInventory.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        playerInventory.setCellOffset(10);
        playerInventory.setMaxCellCount(30);
    }

    @Override
    public void initializeWorkstation(final EntityRef station) {
        WorkstationInventoryComponent workstationInventory = station.getComponent(WorkstationInventoryComponent.class);
        WorkstationInventoryComponent.SlotAssignment inputAssignments = workstationInventory.slotAssignments.get("INPUT");
        WorkstationInventoryComponent.SlotAssignment toolAssignments = workstationInventory.slotAssignments.get("TOOL");
        WorkstationInventoryComponent.SlotAssignment resultAssignments = workstationInventory.slotAssignments.get("OUTPUT");
        WorkstationInventoryComponent.SlotAssignment fluidContainerInputAssignments = workstationInventory.slotAssignments.get("FLUID_CONTAINER_INPUT");
        WorkstationInventoryComponent.SlotAssignment fluidContainerOutputAssignments = workstationInventory.slotAssignments.get("FLUID_CONTAINER_OUTPUT");
        WorkstationInventoryComponent.SlotAssignment fuelAssignments = workstationInventory.slotAssignments.get("FUEL");

        WorkstationInventoryComponent.SlotAssignment fluidInputAssignments = workstationInventory.slotAssignments.get("FLUID_INPUT");

        ingredientsInventory.setTargetEntity(station);
        ingredientsInventory.setCellOffset(inputAssignments.slotStart);
        ingredientsInventory.setMaxCellCount(inputAssignments.slotCount);

        toolsInventory.setTargetEntity(station);
        toolsInventory.setCellOffset(toolAssignments.slotStart);
        toolsInventory.setMaxCellCount(toolAssignments.slotCount);

        fluidContainerInput.setTargetEntity(station);
        fluidContainerInput.setCellOffset(fluidContainerInputAssignments.slotStart);
        fluidContainerInput.setMaxCellCount(fluidContainerInputAssignments.slotCount);

        fluidContainer.setMinX(4);
        fluidContainer.setMaxX(45);

        fluidContainer.setMinY(145);
        fluidContainer.setMaxY(4);

        fluidContainer.setEntity(station);

        final int waterSlot = fluidInputAssignments.slotStart;
        fluidContainer.setSlotNo(waterSlot);

        fluidContainer.bindTooltip(
                new Binding<String>() {
                    @Override
                    public String get() {
                        FluidInventoryComponent fluidInventory = station.getComponent(FluidInventoryComponent.class);
                        final FluidComponent fluid = fluidInventory.fluidSlots.get(waterSlot).getComponent(FluidComponent.class);
                        if (fluid == null) {
                            return "0ml";
                        } else {
                            FluidRegistry fluidRegistry = CoreRegistry.get(FluidRegistry.class);
                            return TeraMath.floorToInt(fluid.volume * 1000) + "ml of " + fluidRegistry.getFluidRenderer(fluid.fluidType).getFluidName();
                        }
                    }

                    @Override
                    public void set(String value) {
                    }
                });

        fluidContainerOutput.setTargetEntity(station);
        fluidContainerOutput.setCellOffset(fluidContainerOutputAssignments.slotStart);
        fluidContainerOutput.setMaxCellCount(fluidContainerOutputAssignments.slotCount);

        setupTemperatureWidget(station);

        burn.bindValue(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        HeatProducerComponent heatProducer = station.getComponent(HeatProducerComponent.class);
                        List<HeatProducerComponent.FuelSourceConsume> consumedFuel = heatProducer.fuelConsumed;
                        if (consumedFuel.size() == 0) {
                            return 0f;
                        }
                        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

                        HeatProducerComponent.FuelSourceConsume lastConsumed = consumedFuel.get(consumedFuel.size() - 1);
                        if (gameTime > lastConsumed.startTime + lastConsumed.burnLength) {
                            return 0f;
                        }
                        return 1f - (1f * (gameTime - lastConsumed.startTime) / lastConsumed.burnLength);
                    }

                    @Override
                    public void set(Float value) {
                    }
                });

        fuelInput.setTargetEntity(station);
        fuelInput.setCellOffset(fuelAssignments.slotStart);
        fuelInput.setMaxCellCount(fuelAssignments.slotCount);

        availableRecipes.setStation(station);

        resultInventory.setTargetEntity(station);
        resultInventory.setCellOffset(resultAssignments.slotStart);
        resultInventory.setMaxCellCount(resultAssignments.slotCount);
    }

    private void setupTemperatureWidget(final EntityRef station) {
        temperature.bindMaxTemperature(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        HeatProducerComponent producer = station.getComponent(HeatProducerComponent.class);
                        return producer.maximumTemperature;
                    }

                    @Override
                    public void set(Float value) {
                    }
                }
        );
        temperature.setMinTemperature(20f);

        temperature.bindTemperature(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        return HeatUtils.calculateHeatForEntity(station, CoreRegistry.get(BlockEntityRegistry.class));
                    }

                    @Override
                    public void set(Float value) {
                    }
                });
        temperature.bindTooltip(
                new Binding<String>() {
                    @Override
                    public String get() {
                        return Math.round(HeatUtils.calculateHeatForEntity(station, CoreRegistry.get(BlockEntityRegistry.class))) + " C";
                    }

                    @Override
                    public void set(String value) {
                    }
                }
        );
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
