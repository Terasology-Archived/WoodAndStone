package org.terasology.was.system;

import org.terasology.durability.DurabilityComponent;
import org.terasology.durability.DurabilityExhaustedEvent;
import org.terasology.durability.OverTimeDurabilityReduceComponent;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.OnBlockItemPlaced;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class LitTorchSystem implements ComponentSystem {
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {OverTimeDurabilityReduceComponent.class})
    public void whenTorchPlaced(OnBlockItemPlaced event, EntityRef item) {
        event.getPlacedBlock().saveComponent(item.getComponent(DurabilityComponent.class));
        event.getPlacedBlock().saveComponent(item.getComponent(OverTimeDurabilityReduceComponent.class));
    }

    @ReceiveEvent(components = {OverTimeDurabilityReduceComponent.class, BlockComponent.class})
    public void whenTorchAsBlockExpires(DurabilityExhaustedEvent event, EntityRef entity) {
        BlockComponent block = entity.getComponent(BlockComponent.class);
        Vector3i position = block.getPosition();
        CoreRegistry.get(WorldProvider.class).setBlock(position, BlockManager.getAir());
        entity.removeComponent(DurabilityComponent.class);
        entity.removeComponent(OverTimeDurabilityReduceComponent.class);
        event.consume();
    }
}
