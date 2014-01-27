package org.terasology.multiBlock;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UniformMultiBlockFormItemRecipe implements MultiBlockFormItemRecipe {
    private EntityFilter activatorFilter;
    private ActivateEventFilter activateEventFilter;
    private EntityFilter blockFilter;
    private int requiredHeight;
    private int requiredMinSize;
    private int requiredMaxSize;
    private String prefab;
    private String replaceBlockUri;

    public UniformMultiBlockFormItemRecipe(EntityFilter activatorFilter, ActivateEventFilter activateEventFilter, EntityFilter blockFilter, Vector3i size,
                                           String multiBlockPrefab, String replaceBlockUri) {
        this.activatorFilter = activatorFilter;
        this.activateEventFilter = activateEventFilter;
        this.blockFilter = blockFilter;
        this.replaceBlockUri = replaceBlockUri;
        this.requiredHeight = size.y;
        this.requiredMinSize = Math.min(size.x, size.z);
        this.requiredMaxSize = Math.max(size.x, size.z);
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

        // Check if the outside boundaries match the required size
        if (Math.min(maxX - minX, maxZ - minZ) + 1 != requiredMinSize) {
            return false;
        }
        if (Math.max(maxX - minX, maxZ - minZ) + 1 != requiredMaxSize) {
            return false;
        }
        if (maxY - minY + 1 != requiredHeight) {
            return false;
        }

        // Now check that all the blocks in the region defined by these boundaries match the criteria
        Region3i multiBlockRegion = Region3i.createBounded(new Vector3i(minX, minY, minZ), new Vector3i(maxX, maxY, maxZ));
        for (Vector3i blockLocation : multiBlockRegion) {
            if (!blockFilter.accepts(blockEntityRegistry.getBlockEntityAt(blockLocation))) {
                return false;
            }
        }

        // Ok, we got matching blocks now we can form the multi-block
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        Block block = blockManager.getBlock(replaceBlockUri);

        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        // First, replace the blocks in world
        PlaceBlocks placeBlocksEvent = new PlaceBlocks(getBlockLocationMap(multiBlockRegion, block), event.getInstigator());
        worldProvider.getWorldEntity().send(placeBlocksEvent);

        if (placeBlocksEvent.isConsumed()) {
            return false;
        }

        // Create the block region entity
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        EntityRef multiBlockEntity = entityManager.create(prefab);
        multiBlockEntity.addComponent(new BlockRegionComponent(multiBlockRegion));
        multiBlockEntity.addComponent(new LocationComponent(multiBlockRegion.center()));

        multiBlockEntity.send(new MultiBlockFormed(event.getInstigator()));

        return true;
    }

    private Map<Vector3i, Block> getBlockLocationMap(Region3i multiBlockRegion, Block block) {
        Map<Vector3i, Block> blocksToPlace = new HashMap<>();
        for (Vector3i location : multiBlockRegion) {
            blocksToPlace.put(location, block);
        }
        return blocksToPlace;
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
