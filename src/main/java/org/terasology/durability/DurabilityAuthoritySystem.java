/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.durability;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.NoHealthEvent;
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
                    tool.send(new ReduceDurabilityEvent(1));
                }
            }
        }
    }

    @ReceiveEvent(components = {DurabilityComponent.class})
    public void reduceDurability(ReduceDurabilityEvent event, EntityRef entity) {
        DurabilityComponent durabilityComponent = entity.getComponent(DurabilityComponent.class);
        durabilityComponent.durability--;
        entity.saveComponent(durabilityComponent);

        entity.send(new DurabilityReducedEvent());
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
        return true;
    }
}
