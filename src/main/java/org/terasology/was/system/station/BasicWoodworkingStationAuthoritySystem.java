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
package org.terasology.was.system.station;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.was.event.CraftingStationFormed;
import org.terasology.workstation.component.CraftingStationMaterialComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import javax.vecmath.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BasicWoodworkingStationAuthoritySystem implements ComponentSystem {
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;
    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {ItemComponent.class})
    public void createCraftingStation(ActivateEvent event, EntityRef item) {
        if (event.getTarget() != null) {
            Side side = Side.inDirection(event.getHitNormal());
            if (side == Side.TOP) {
                CraftingStationMaterialComponent stationMaterial = event.getTarget().getComponent(CraftingStationMaterialComponent.class);
                if (stationMaterial != null) {
                    String toolType = stationMaterial.toolType;

                    ItemComponent component = item.getComponent(ItemComponent.class);
                    if (component != null) {
                        BlockDamageModifierComponent blockDamage = component.damageType.getComponent(BlockDamageModifierComponent.class);
                        if (blockDamage != null && blockDamage.materialDamageMultiplier.containsKey(toolType)) {
                            processBlockStructure(event.getInstigator(), event.getTarget(), stationMaterial.stationBlockType, stationMaterial.stationType);
                        }
                    }
                }
            }
        }
    }

    private void processBlockStructure(EntityRef character, EntityRef matchingBlock, String stationBlockType, String stationType) {
        LocationComponent location = matchingBlock.getComponent(LocationComponent.class);
        Vector3f position = location.getWorldPosition();
        EntityRef otherBlock = findOtherMatchingBlock(position, stationType);
        if (otherBlock != null) {
            putBasicWoodworkingStation(character, matchingBlock, otherBlock, stationBlockType, stationType);
        }
    }

    private void putBasicWoodworkingStation(EntityRef character, EntityRef block1, EntityRef block2, String stationBlockType, String stationType) {
        Block woodStationBlock = blockManager.getBlock(stationBlockType);

        Vector3i block1Position = block1.getComponent(BlockComponent.class).getPosition();
        Vector3i block2Position = block2.getComponent(BlockComponent.class).getPosition();

        worldProvider.setBlock(block1Position, woodStationBlock);
        worldProvider.setBlock(block2Position, woodStationBlock);

        final EntityRef multiBlockEntity = entityManager.create(stationType);
        Region3i region = Region3i.createBounded(block1Position, block2Position);
        multiBlockEntity.addComponent(new BlockRegionComponent(region));
        multiBlockEntity.addComponent(new LocationComponent(region.center()));

        character.send(new CraftingStationFormed(stationType));
    }

    private EntityRef findOtherMatchingBlock(Vector3f position, String stationType) {
        EntityRef entity = blockEntityRegistry.getBlockEntityAt(new Vector3f(position.x + 1, position.y, position.z));
        if (checkIfBlockIsBasicWoodcraftStationPotential(entity, stationType)) {
            return entity;
        }
        entity = blockEntityRegistry.getBlockEntityAt(new Vector3f(position.x - 1, position.y, position.z));
        if (checkIfBlockIsBasicWoodcraftStationPotential(entity, stationType)) {
            return entity;
        }
        entity = blockEntityRegistry.getBlockEntityAt(new Vector3f(position.x, position.y, position.z + 1));
        if (checkIfBlockIsBasicWoodcraftStationPotential(entity, stationType)) {
            return entity;
        }
        entity = blockEntityRegistry.getBlockEntityAt(new Vector3f(position.x, position.y, position.z - 1));
        if (checkIfBlockIsBasicWoodcraftStationPotential(entity, stationType)) {
            return entity;
        }
        return null;
    }

    private boolean checkIfBlockIsBasicWoodcraftStationPotential(EntityRef block, String stationType) {
        CraftingStationMaterialComponent craftingStationPotential = block.getComponent(CraftingStationMaterialComponent.class);
        return craftingStationPotential != null && craftingStationPotential.stationType.equals(stationType);
    }
}
