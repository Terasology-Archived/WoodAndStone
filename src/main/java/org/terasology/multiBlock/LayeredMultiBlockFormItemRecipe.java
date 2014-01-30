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
package org.terasology.multiBlock;

import org.terasology.anotherWorld.util.Filter;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class LayeredMultiBlockFormItemRecipe implements MultiBlockFormItemRecipe {
    private Filter<EntityRef> itemFilter;
    private Filter<Vector2i> sizeFilter;
    private Filter<ActivateEvent> activateEventFilter;
    private String prefab;
    private Callback callback;

    private List<LayerDefinition> layerDefinitions = new ArrayList<>();

    public LayeredMultiBlockFormItemRecipe(Filter<EntityRef> itemFilter, Filter<Vector2i> sizeFilter,
                                           Filter<ActivateEvent> activateEventFilter, String prefab, Callback callback) {
        this.itemFilter = itemFilter;
        this.sizeFilter = sizeFilter;
        this.activateEventFilter = activateEventFilter;
        this.prefab = prefab;
        this.callback = callback;
    }

    @Override
    public boolean isActivator(EntityRef item) {
        return itemFilter.accepts(item);
    }

    public void addLayer(int minHeight, int maxHeight, Filter<EntityRef> entityFilter, Block blockToReplaceWith) {
        if (minHeight > maxHeight || minHeight < 0) {
            throw new IllegalArgumentException("Invalid values for minHeight and maxHeight");
        }
        layerDefinitions.add(new LayerDefinition(minHeight, maxHeight, entityFilter, blockToReplaceWith));
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

        for (int i = 0; i < layerDefinitions.size(); i++) {
            LayerDefinition layerDefinition = layerDefinitions.get(i);
            if (layerDefinition.entityFilter.accepts(target)) {
                if (processDetectionForLayer(i, targetBlock.getPosition())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean processDetectionForLayer(int layerIndex, Vector3i basePosition) {
        BlockEntityRegistry blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        LayerDefinition layerDefinition = layerDefinitions.get(layerIndex);
        Filter<EntityRef> entityFilter = layerDefinition.entityFilter;
        int minX = getLastMatchingInDirection(blockEntityRegistry, entityFilter, basePosition, Vector3i.east()).x;
        int maxX = getLastMatchingInDirection(blockEntityRegistry, entityFilter, basePosition, Vector3i.west()).x;
        int minZ = getLastMatchingInDirection(blockEntityRegistry, entityFilter, basePosition, Vector3i.south()).z;
        int maxZ = getLastMatchingInDirection(blockEntityRegistry, entityFilter, basePosition, Vector3i.north()).z;

        // First check if the size is accepted at all
        Vector2i multiBlockHorizontalSize = new Vector2i(maxX - minX + 1, maxZ - minZ + 1);
        if (!sizeFilter.accepts(multiBlockHorizontalSize)) {
            return false;
        }

        int minY = getLastMatchingInDirection(blockEntityRegistry, entityFilter, basePosition, Vector3i.down()).z;
        int maxY = getLastMatchingInDirection(blockEntityRegistry, entityFilter, basePosition, Vector3i.up()).z;

        // Then check if this layer height is accepted
        int layerHeight = maxY - minY + 1;
        if (layerDefinition.minHeight > layerHeight || layerDefinition.maxHeight < layerHeight) {
            return false;
        }

        int[] layerHeights = new int[layerDefinitions.size()];
        layerHeights[layerIndex] = layerHeight;

        // Go up the stack and match layers
        int lastLayerYUp = maxY;
        for (int i = layerIndex + 1; i < layerDefinitions.size(); i++) {
            LayerDefinition upLayerDefinition = layerDefinitions.get(i);
            int lastMatchingY = getLastMatchingInDirection(blockEntityRegistry, upLayerDefinition.entityFilter,
                    new Vector3i(basePosition.x, lastLayerYUp, basePosition.z), Vector3i.up()).y;
            // Layer height
            int upLayerHeight = lastMatchingY - lastLayerYUp;
            if (upLayerDefinition.minHeight > upLayerHeight || upLayerDefinition.maxHeight < upLayerHeight) {
                return false;
            }
            layerHeights[i] = upLayerHeight;
            lastLayerYUp += upLayerHeight;
        }

        // Go down the stack and match layers
        int lastLayerYDown = minY;
        for (int i = layerIndex - 1; i >= 0; i--) {
            LayerDefinition downLayerDefinition = layerDefinitions.get(i);
            int lastMatchingY = getLastMatchingInDirection(blockEntityRegistry, downLayerDefinition.entityFilter,
                    new Vector3i(basePosition.x, lastLayerYUp, basePosition.z), Vector3i.down()).y;
            // Layer height
            int downLayerHeight = lastLayerYDown - lastMatchingY;
            if (downLayerDefinition.minHeight > downLayerHeight || downLayerDefinition.maxHeight < downLayerHeight) {
                return false;
            }
            layerHeights[i] = downLayerHeight;
            lastLayerYDown -= downLayerHeight;
        }

        // We detected the boundaries of the possible multi-block, now we need to validate that all blocks in the region (for each layer) match
        int validationY = lastLayerYDown;
        Map<Vector3i, Block> blocksToReplace = new HashMap<>();
        for (int i = 0; i < layerHeights.length; i++) {
            if (layerHeights[i] > 0) {
                Region3i layerRegion = Region3i.createBounded(new Vector3i(minX, validationY, minZ),
                        new Vector3i(maxX, validationY + layerHeights[i] - 1, maxZ));
                LayerDefinition validateLayerDefinition = layerDefinitions.get(i);
                for (Vector3i position : layerRegion) {
                    if (!validateLayerDefinition.entityFilter.accepts(blockEntityRegistry.getBlockEntityAt(position))) {
                        return false;
                    }
                    blocksToReplace.put(position, validateLayerDefinition.blockToReplaceWith);
                }
                validationY += layerHeights[i];
            }
        }

        // Ok, now we can replace the blocks
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        EntityRef worldEntity = worldProvider.getWorldEntity();
        PlaceBlocks event = new PlaceBlocks(blocksToReplace);
        worldEntity.send(event);

        if (event.isConsumed()) {
            return false;
        }

        Region3i multiBlockRegion = Region3i.createBounded(new Vector3i(minX, lastLayerYDown, minZ), new Vector3i(maxX, lastLayerYUp, maxZ));

        // Create the block region entity
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        EntityRef multiBlockEntity = entityManager.create(prefab);
        multiBlockEntity.addComponent(new BlockRegionComponent(multiBlockRegion));
        multiBlockEntity.addComponent(new LocationComponent(multiBlockRegion.center()));

        callback.multiBlockFormed(multiBlockEntity, multiBlockHorizontalSize, layerHeights);

        multiBlockEntity.send(new MultiBlockFormed(event.getInstigator()));

        return true;
    }

    private Vector3i getLastMatchingInDirection(BlockEntityRegistry blockEntityRegistry, Filter<EntityRef> entityFilter, Vector3i location, Vector3i direction) {
        Vector3i result = location;
        while (true) {
            Vector3i testedLocation = new Vector3i(result.x + direction.x, result.y + direction.y, result.z + direction.z);
            EntityRef blockEntityAt = blockEntityRegistry.getBlockEntityAt(testedLocation);
            if (!entityFilter.accepts(blockEntityAt)) {
                return result;
            }
            result = testedLocation;
        }
    }

    private static final class LayerDefinition {
        private int minHeight;
        private int maxHeight;
        private Filter<EntityRef> entityFilter;
        private Block blockToReplaceWith;

        private LayerDefinition(int minHeight, int maxHeight, Filter<EntityRef> entityFilter, Block blockToReplaceWith) {
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
            this.entityFilter = entityFilter;
            this.blockToReplaceWith = blockToReplaceWith;
        }
    }

    public interface Callback {
        void multiBlockFormed(EntityRef entity, Vector2i size, int[] layerSetup);
    }
}
