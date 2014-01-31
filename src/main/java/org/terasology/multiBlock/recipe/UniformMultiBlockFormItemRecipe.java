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
package org.terasology.multiBlock.recipe;

import org.terasology.anotherWorld.util.Filter;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.multiBlock.MultiBlockCallback;
import org.terasology.multiBlock.MultiBlockFormed;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UniformMultiBlockFormItemRecipe implements MultiBlockFormItemRecipe {
    private Filter<EntityRef> activatorFilter;
    private Filter<ActivateEvent> activateEventFilter;
    private Filter<EntityRef> blockFilter;
    private Filter<Vector3i> sizeFilter;
    private String prefab;
    private MultiBlockCallback<Void> callback;

    public UniformMultiBlockFormItemRecipe(Filter<EntityRef> activatorFilter, Filter<ActivateEvent> activateEventFilter,
                                           Filter<EntityRef> blockFilter, Filter<Vector3i> sizeFilter,
                                           String multiBlockPrefab, MultiBlockCallback<Void> callback) {
        this.activatorFilter = activatorFilter;
        this.activateEventFilter = activateEventFilter;
        this.blockFilter = blockFilter;
        this.sizeFilter = sizeFilter;
        this.callback = callback;
        this.prefab = multiBlockPrefab;
    }

    @Override
    public boolean isActivator(EntityRef item) {
        return activatorFilter.accepts(item);
    }

    @Override
    public boolean processActivation(ActivateEvent event) {
        if (!activateEventFilter.accepts(event)) {
            return false;
        }

        EntityRef target = event.getTarget();
        BlockComponent targetBlock = target.getComponent(BlockComponent.class);
        if (targetBlock == null) {
            return false;
        }

        if (!blockFilter.accepts(target)) {
            return false;
        }

        BlockEntityRegistry blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);

        Vector3i blockPosition = targetBlock.getPosition();
        int minX = getLastMatchingInDirection(blockEntityRegistry, blockPosition, Vector3i.east()).x;
        int maxX = getLastMatchingInDirection(blockEntityRegistry, blockPosition, Vector3i.west()).x;
        int minY = getLastMatchingInDirection(blockEntityRegistry, blockPosition, Vector3i.down()).y;
        int maxY = getLastMatchingInDirection(blockEntityRegistry, blockPosition, Vector3i.up()).y;
        int minZ = getLastMatchingInDirection(blockEntityRegistry, blockPosition, Vector3i.south()).z;
        int maxZ = getLastMatchingInDirection(blockEntityRegistry, blockPosition, Vector3i.north()).z;

        Region3i multiBlockRegion = Region3i.createBounded(new Vector3i(minX, minY, minZ), new Vector3i(maxX, maxY, maxZ));

        // Check if the size is accepted
        if (!sizeFilter.accepts(multiBlockRegion.size())) {
            return false;
        }

        // Now check that all the blocks in the region defined by these boundaries match the criteria
        for (Vector3i blockLocation : multiBlockRegion) {
            if (!blockFilter.accepts(blockEntityRegistry.getBlockEntityAt(blockLocation))) {
                return false;
            }
        }

        // Ok, we got matching blocks now we can form the multi-block
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        Map<Vector3i, Block> replacementMap = callback.getReplacementMap(multiBlockRegion, null);

        // First, replace the blocks in world
        PlaceBlocks placeBlocksEvent = new PlaceBlocks(replacementMap, event.getInstigator());
        worldProvider.getWorldEntity().send(placeBlocksEvent);

        if (placeBlocksEvent.isConsumed()) {
            return false;
        }

        // Create the block region entity
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        EntityRef multiBlockEntity = entityManager.create(prefab);
        multiBlockEntity.addComponent(new BlockRegionComponent(multiBlockRegion));
        multiBlockEntity.addComponent(new LocationComponent(multiBlockRegion.center()));

        callback.multiBlockFormed(multiBlockRegion, multiBlockEntity, null);

        multiBlockEntity.send(new MultiBlockFormed(event.getInstigator()));

        return true;
    }

    private Vector3i getLastMatchingInDirection(BlockEntityRegistry blockEntityRegistry, Vector3i location, Vector3i direction) {
        Vector3i result = location;
        while (true) {
            Vector3i testedLocation = new Vector3i(result.x + direction.x, result.y + direction.y, result.z + direction.z);
            EntityRef blockEntityAt = blockEntityRegistry.getBlockEntityAt(testedLocation);
            if (!blockFilter.accepts(blockEntityAt)) {
                return result;
            }
            result = testedLocation;
        }
    }
}
