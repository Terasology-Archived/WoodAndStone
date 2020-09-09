// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.system;

import org.terasology.durability.DurabilityComponent;
import org.terasology.durability.DurabilityExhaustedEvent;
import org.terasology.durability.OverTimeDurabilityReduceComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.OnBlockItemPlaced;
import org.terasology.engine.world.block.items.OnBlockToItem;
import org.terasology.math.geom.Vector3i;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class LitTorchSystem extends BaseComponentSystem {
    @In
    private BlockManager blockManager;

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
        CoreRegistry.get(WorldProvider.class).setBlock(position, blockManager.getBlock(BlockManager.AIR_ID));
        entity.removeComponent(DurabilityComponent.class);
        entity.removeComponent(OverTimeDurabilityReduceComponent.class);
        event.consume();
    }
}
