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
package org.terasology.was.system;

import org.terasology.durability.DurabilityComponent;
import org.terasology.durability.DurabilityExhaustedEvent;
import org.terasology.durability.OverTimeDurabilityReduceComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.OnBlockItemPlaced;
import org.terasology.world.block.items.OnBlockToItem;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class LitTorchSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void whenTorchPlaced(OnBlockItemPlaced event, EntityRef item,
                                OverTimeDurabilityReduceComponent overTimeDurabilityReduceComponent,
                                DurabilityComponent itemDurability) {
        EntityRef blockEntity = event.getPlacedBlock();
        DurabilityComponent durability = blockEntity.getComponent(DurabilityComponent.class);
        durability.durability = itemDurability.durability;
        blockEntity.saveComponent(durability);
    }

    @ReceiveEvent
    public void whenTorchRemoved(OnBlockToItem event, EntityRef block,
                                 OverTimeDurabilityReduceComponent overTimeDurabilityReduceComponent,
                                 DurabilityComponent blockDurability) {
        EntityRef itemEntity = event.getItem();
        DurabilityComponent durability = itemEntity.getComponent(DurabilityComponent.class);
        durability.durability = blockDurability.durability;
        itemEntity.saveComponent(durability);
    }

    @ReceiveEvent
    public void whenTorchAsBlockExpires(DurabilityExhaustedEvent event, EntityRef entity,
                                        OverTimeDurabilityReduceComponent overTimeDurabilityReduceComponent,
                                        BlockComponent block) {
        Vector3i position = block.getPosition();
        CoreRegistry.get(WorldProvider.class).setBlock(position, BlockManager.getAir());
        entity.removeComponent(DurabilityComponent.class);
        entity.removeComponent(OverTimeDurabilityReduceComponent.class);
        event.consume();
    }
}
