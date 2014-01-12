package org.terasology.was.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.EntityBeingGenerated;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.NoHealthEvent;
import org.terasology.was.component.DurabilityComponent;
import org.terasology.was.event.DurabilityReducedEvent;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.entity.BlockDamageComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class DurabilityAuthoritySystem implements ComponentSystem {
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void addDurabilityToTools(EntityBeingGenerated event, EntityRef entity) {
        String prefabName = event.getPrefabName();
        if (prefabName != null) {
            if (prefabName.equals("Core:axe") || prefabName.equals("Core:pickaxe")) {
                DurabilityComponent durabilityComponent = new DurabilityComponent();
                durabilityComponent.maxDurability = 20;
                durabilityComponent.durability = 20;
                event.addComponent(durabilityComponent);
            }
        }
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void reduceItemDurability(NoHealthEvent event, EntityRef entity) {
        EntityRef tool = event.getTool();
        DurabilityComponent durabilityComponent = tool.getComponent(DurabilityComponent.class);
        if (durabilityComponent != null) {
            BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
            if (blockComponent != null) {
                Block block = blockComponent.getBlock();
                Iterable<String> categoriesIterator = block.getBlockFamily().getCategories();
                if (canBeDestroyedByBlockDamage(categoriesIterator, event.getDamageType())) {
                    // It was the right tool for the job, so reduce the durability
                    durabilityComponent.durability--;
                    tool.saveComponent(durabilityComponent);

                    tool.send(new DurabilityReducedEvent());
                }
            }
        }
    }

    @ReceiveEvent(components = {DurabilityComponent.class})
    public void destroyItemOnZeroDurability(DurabilityReducedEvent event, EntityRef entity) {
        DurabilityComponent durability = entity.getComponent(DurabilityComponent.class);
        if (durability.durability == 0) {
            entity.destroy();
        }
    }

    private boolean canBeDestroyedByBlockDamage(Iterable<String> categoriesIterator, Prefab damageType) {
        if (categoriesIterator.iterator().hasNext()) {
            BlockDamageComponent blockDamage = damageType.getComponent(BlockDamageComponent.class);
            if (blockDamage == null)
                return false;
            for (String category : categoriesIterator) {
                if (blockDamage.materialDamageMultiplier.containsKey(category))
                    return true;
            }
            return false;
        }
        return true;
    }
}
