// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.system;

import org.joml.Vector3i;
import org.terasology.durability.DurabilityComponent;
import org.terasology.durability.DurabilityExhaustedEvent;
import org.terasology.durability.OverTimeDurabilityReduceComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
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
        Vector3i position = block.getPosition(new Vector3i());
        CoreRegistry.get(WorldProvider.class).setBlock(position, blockManager.getBlock(BlockManager.AIR_ID));
        entity.removeComponent(DurabilityComponent.class);
        entity.removeComponent(OverTimeDurabilityReduceComponent.class);
        event.consume();
    }
}
