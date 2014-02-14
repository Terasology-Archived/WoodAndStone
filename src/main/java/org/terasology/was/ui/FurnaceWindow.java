package org.terasology.was.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.was.heat.HeatUtils;
import org.terasology.workstation.ui.WorkstationUI;
import org.terasology.world.BlockEntityRegistry;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class FurnaceWindow extends CoreScreenLayer implements WorkstationUI {
    private InventoryGrid input;
    private InventoryGrid fuel;
    private InventoryGrid output;
    private UILabel heatLabel;

    @Override
    public void initialise() {
        input = find("input", InventoryGrid.class);
        fuel = find("fuel", InventoryGrid.class);
        output = find("output", InventoryGrid.class);

        InventoryGrid player = find("player", InventoryGrid.class);
        player.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        player.setCellOffset(10);
        player.setMaxCellCount(30);

        heatLabel = find("heat", UILabel.class);
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

        heatLabel.bindText(
                new Binding<String>() {
                    @Override
                    public String get() {
                        float heat = HeatUtils.calculateHeatForEntity(workstation, CoreRegistry.get(BlockEntityRegistry.class));
                        return "Temperature: " + (Math.round(HeatUtils.heatToCelsius(heat))) + "C";
                    }

                    @Override
                    public void set(String value) {
                    }
                });
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
