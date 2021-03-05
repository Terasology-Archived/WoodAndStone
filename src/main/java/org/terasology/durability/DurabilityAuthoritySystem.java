// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.durability;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.entity.damage.BlockDamageModifierComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class DurabilityAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private Time time;
    @In
    private EntityManager entityManager;

    private long tickLength = 5000;
    private long lastModified;

    @Override
    public void update(float delta) {
        long gameTimeInMs = time.getGameTimeInMs();
        if (lastModified + tickLength < gameTimeInMs) {
            for (EntityRef entityRef : entityManager.getEntitiesWith(OverTimeDurabilityReduceComponent.class, DurabilityComponent.class)) {
                entityRef.send(new ReduceDurabilityEvent(1));
            }

            lastModified = gameTimeInMs;
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void reduceItemDurability(DestroyEvent event, EntityRef entity,
                                     BlockComponent blockComponent) {
        EntityRef tool = event.getDirectCause();
        DurabilityComponent durabilityComponent = tool.getComponent(DurabilityComponent.class);
        if (durabilityComponent != null) {
            Block block = blockComponent.getBlock();
            Iterable<String> categoriesIterator = block.getBlockFamily().getCategories();
            if (isTheRightTool(categoriesIterator, event.getDamageType())) {
                // It was the right tool for the job, so reduce the durability
                tool.send(new ReduceDurabilityEvent(1));
            }
        }
    }

    @ReceiveEvent
    public void reduceDurability(ReduceDurabilityEvent event, EntityRef entity,
                                 DurabilityComponent durability) {
        durability.durability -= event.getReduceBy();
        if (durability.durability < 0) {
            durability.durability = 0;
        }
        entity.saveComponent(durability);

        entity.send(new DurabilityReducedEvent());
    }

    @ReceiveEvent
    public void checkIfDurabilityExhausted(DurabilityReducedEvent event, EntityRef entity,
                                           DurabilityComponent durability) {
        if (durability.durability == 0) {
            entity.send(new DurabilityExhaustedEvent());
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void destroyItemOnZeroDurability(DurabilityExhaustedEvent event, EntityRef entity,
                                            DurabilityComponent durabilityComponent) {
        entity.destroy();
    }

    private boolean isTheRightTool(Iterable<String> categoriesIterator, Prefab damageType) {
        if (categoriesIterator.iterator().hasNext()) {
            BlockDamageModifierComponent blockDamage = damageType.getComponent(BlockDamageModifierComponent.class);
            if (blockDamage == null) {
                return false;
            }
            for (String category : categoriesIterator) {
                if (blockDamage.materialDamageMultiplier.containsKey(category)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}
