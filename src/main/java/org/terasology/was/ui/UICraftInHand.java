package org.terasology.was.ui;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.WindowListener;
import org.terasology.rendering.gui.widgets.UIInventoryCell;
import org.terasology.rendering.gui.windows.UIScreenInventory;
import org.terasology.was.event.UserClosedCraftInHandUI;

import javax.vecmath.Vector2f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UICraftInHand extends UIScreenInventory {
    private EntityRef inventory1;
    private EntityRef inventory2;
    private EntityRef inventory3;

    private EntityManager entityManager;
    private SlotBasedInventoryManager inventoryManager;

    private EntityRef characterEntity;

    public UICraftInHand() {
        super();
        entityManager = CoreRegistry.get(EntityManager.class);
        inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);

        addWindowListener(
                new WindowListener() {
                    @Override
                    public void initialise(UIDisplayElement element) {
                        windowOpened();
                    }

                    @Override
                    public void shutdown(UIDisplayElement element) {
                        windowClosed();
                    }
                }
        );
    }

    public void setCharacterEntity(EntityRef characterEntity) {
        this.characterEntity = characterEntity;
    }

    public void windowOpened() {
        inventory1 = entityManager.create();
        InventoryComponent inventory1Component = new InventoryComponent(1);
        inventory1.addComponent(inventory1Component);

        inventory2 = entityManager.create();
        InventoryComponent inventory2Component = new InventoryComponent(1);
        inventory2.addComponent(inventory2Component);

        inventory3 = entityManager.create();
        InventoryComponent inventory3Component = new InventoryComponent(1);
        inventory3.addComponent(inventory3Component);

        UIInventoryCell inventory1Cell = new UIInventoryCell(inventory1, 0, new Vector2f(48, 48));
        inventory1Cell.setGhost(true);
        inventory1Cell.setPosition(new Vector2f(-50, 0));
        inventory1Cell.setVerticalAlign(EVerticalAlign.CENTER);
        inventory1Cell.setHorizontalAlign(EHorizontalAlign.CENTER);

        UIInventoryCell inventory2Cell = new UIInventoryCell(inventory2, 0, new Vector2f(48, 48));
        inventory2Cell.setGhost(true);
        inventory2Cell.setPosition(new Vector2f(0, 0));
        inventory2Cell.setVerticalAlign(EVerticalAlign.CENTER);
        inventory2Cell.setHorizontalAlign(EHorizontalAlign.CENTER);

        UIInventoryCell inventory3Cell = new UIInventoryCell(inventory3, 0, new Vector2f(48, 48));
        inventory3Cell.setGhost(true);
        inventory3Cell.setPosition(new Vector2f(50, 0));
        inventory3Cell.setVerticalAlign(EVerticalAlign.CENTER);
        inventory3Cell.setHorizontalAlign(EHorizontalAlign.CENTER);

        addDisplayElement(inventory1Cell);
        addDisplayElement(inventory2Cell);
        addDisplayElement(inventory3Cell);
    }

    public void windowClosed() {
        characterEntity.send(
                new UserClosedCraftInHandUI(
                        inventoryManager.getItemInSlot(inventory1, 0),
                        inventoryManager.getItemInSlot(inventory2, 0),
                        inventoryManager.getItemInSlot(inventory3, 0)));

        inventory1.destroy();
        inventory2.destroy();
    }
}
