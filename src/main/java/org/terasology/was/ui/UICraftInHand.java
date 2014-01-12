package org.terasology.was.ui;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryComponent;
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
    private EntityRef handleInventory;
    private EntityRef toolHeadInventory;

    private EntityManager entityManager;

    private EntityRef characterEntity;

    public UICraftInHand() {
        super();
        entityManager = CoreRegistry.get(EntityManager.class);

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
        handleInventory = entityManager.create();
        InventoryComponent handleInventoryComponent = new InventoryComponent(1);
        handleInventory.addComponent(handleInventoryComponent);

        toolHeadInventory = entityManager.create();
        InventoryComponent toolHeadInventoryComponent = new InventoryComponent(1);
        toolHeadInventory.addComponent(toolHeadInventoryComponent);

        UIInventoryCell handleCell = new UIInventoryCell(handleInventory, 0, new Vector2f(48, 48));
        handleCell.setGhost(true);
        handleCell.setPosition(new Vector2f(-50, 0));
        handleCell.setVerticalAlign(EVerticalAlign.CENTER);
        handleCell.setHorizontalAlign(EHorizontalAlign.CENTER);

        UIInventoryCell toolHeadCell = new UIInventoryCell(toolHeadInventory, 0, new Vector2f(48, 48));
        toolHeadCell.setGhost(true);
        toolHeadCell.setPosition(new Vector2f(0, 0));
        toolHeadCell.setVerticalAlign(EVerticalAlign.CENTER);
        toolHeadCell.setHorizontalAlign(EHorizontalAlign.CENTER);

        addDisplayElement(handleCell);
        addDisplayElement(toolHeadCell);
    }

    public void windowClosed() {
        characterEntity.send(
                new UserClosedCraftInHandUI(
                        handleInventory.getComponent(InventoryComponent.class).itemSlots.get(0),
                        toolHeadInventory.getComponent(InventoryComponent.class).itemSlots.get(0)));

        handleInventory.destroy();
        toolHeadInventory.destroy();
    }
}
