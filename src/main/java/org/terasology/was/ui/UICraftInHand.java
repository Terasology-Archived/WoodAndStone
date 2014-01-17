package org.terasology.was.ui;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.WindowListener;
import org.terasology.rendering.gui.windows.UIScreenInventory;
import org.terasology.was.system.hand.CraftInHandRecipeRegistry;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UICraftInHand extends UIScreenInventory {
    private EntityManager entityManager;
    private SlotBasedInventoryManager inventoryManager;

    private EntityRef characterEntity;

    private UIAvailableInHandRecipesDisplay allRecipesDisplay;

    public UICraftInHand() {
        super();
        setId("WoodAndStone:CraftInHand");
        setModal(true);
        entityManager = CoreRegistry.get(EntityManager.class);
        inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);

        addWindowListener(
                new WindowListener() {
                    @Override
                    public void initialise(UIDisplayElement element) {
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
        updateRecipes();
    }

    public void updateRecipes() {
        if (allRecipesDisplay != null) {
            removeDisplayElement(allRecipesDisplay);
            allRecipesDisplay.dispose();
        }

        CraftInHandRecipeRegistry registry = CoreRegistry.get(CraftInHandRecipeRegistry.class);

        allRecipesDisplay = new UIAvailableInHandRecipesDisplay(registry, characterEntity);
        allRecipesDisplay.setHorizontalAlign(EHorizontalAlign.CENTER);
        allRecipesDisplay.setVerticalAlign(EVerticalAlign.TOP);
        addDisplayElement(allRecipesDisplay);

        layout();
    }

    public void windowClosed() {

    }
}
