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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Side;
import org.terasology.was.component.CraftingStationMaterialComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.entity.BlockDamageComponent;

import javax.vecmath.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BasicWoodworkingStationAuthoritySystem implements ComponentSystem {
    @In
    private BlockEntityRegistry blockEntityRegistry;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {ItemComponent.class})
    public void createBasicWoodworkingStation(ActivateEvent event, EntityRef item) {
        if (event.getTarget() != null) {
            Side side = Side.inDirection(event.getDirection());
            if (side == Side.TOP) {
                ItemComponent component = item.getComponent(ItemComponent.class);
                if (component != null) {
                    BlockDamageComponent blockDamage = component.damageType.getComponent(BlockDamageComponent.class);
                    if (blockDamage.materialDamageMultiplier.containsKey("wood")) {
                        if (checkIfBlockIsBasicWoodcraftStationPotential(event.getTarget())) {
                            processBlockStructure(event.getTarget());
                        }
                    }
                }
            }
        }
    }

    private void processBlockStructure(EntityRef matchingBlock) {
        LocationComponent location = matchingBlock.getComponent(LocationComponent.class);
        Vector3f position = location.getWorldPosition();
        EntityRef otherBlock = findOtherMatchingBlock(position);
        if (otherBlock != null) {
            putBasicWoodworkingStation(matchingBlock, otherBlock);
        }
    }

    private void putBasicWoodworkingStation(EntityRef block1, EntityRef block2) {

    }

    private EntityRef findOtherMatchingBlock(Vector3f position) {
        EntityRef entity = blockEntityRegistry.getBlockEntityAt(new Vector3f(position.x + 1, position.y, position.z));
        if (checkIfBlockIsBasicWoodcraftStationPotential(entity))
            return entity;
        entity = blockEntityRegistry.getBlockEntityAt(new Vector3f(position.x - 1, position.y, position.z));
        if (checkIfBlockIsBasicWoodcraftStationPotential(entity))
            return entity;
        entity = blockEntityRegistry.getBlockEntityAt(new Vector3f(position.x, position.y, position.z + 1));
        if (checkIfBlockIsBasicWoodcraftStationPotential(entity))
            return entity;
        entity = blockEntityRegistry.getBlockEntityAt(new Vector3f(position.x, position.y, position.z - 1));
        if (checkIfBlockIsBasicWoodcraftStationPotential(entity))
            return entity;
        return null;
    }

    private boolean checkIfBlockIsBasicWoodcraftStationPotential(EntityRef block) {
        CraftingStationMaterialComponent craftingStationPotential = block.getComponent(CraftingStationMaterialComponent.class);
        return craftingStationPotential != null && craftingStationPotential.type.equals("basicWoodcrafting");
    }
}
