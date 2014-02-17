package org.terasology.was.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.workstation.ui.WorkstationUI;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CookingStationWindow extends CoreScreenLayer implements WorkstationUI {

    private InventoryGrid fluidContainerInput;
    private InventoryGrid fluidContainerOutput;
    private FluidContainerWidget fluidContainer;

    @Override
    public void initialise() {
        fluidContainerInput = find("fluidContainerInput", InventoryGrid.class);
        fluidContainerOutput = find("fluidContainerOutput", InventoryGrid.class);
        fluidContainer = find("fluidContainer", FluidContainerWidget.class);

        InventoryGrid playerInventory = find("playerInventory", InventoryGrid.class);

        playerInventory.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        playerInventory.setCellOffset(10);
        playerInventory.setMaxCellCount(30);
    }

    @Override
    public void initializeWorkstation(EntityRef workstation) {
        fluidContainerInput.setTargetEntity(workstation);
        fluidContainerInput.setCellOffset(0);
        fluidContainerInput.setMaxCellCount(1);

        fluidContainerOutput.setTargetEntity(workstation);
        fluidContainerOutput.setCellOffset(1);
        fluidContainerOutput.setMaxCellCount(1);

        fluidContainer.setMinX(4);
        fluidContainer.setMaxX(45);

        fluidContainer.setMinY(145);
        fluidContainer.setMaxY(4);

        fluidContainer.setEntity(workstation);
        fluidContainer.setSlotNo(0);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
