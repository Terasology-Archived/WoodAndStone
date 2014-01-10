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

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.items.BeforeBlockToItem;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class WoodAndStoneBlockDrops implements ComponentSystem {
    private WorldProvider worldProvider;
    private EntityManager entityManager;
    private BlockManager blockManager;
    private Map<BlockUri, Drop> blockDrops = new HashMap<>();

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        entityManager = CoreRegistry.get(EntityManager.class);
        blockManager = CoreRegistry.get(BlockManager.class);

        blockDrops.put(new BlockUri("Core", "Grass"),
                new Drop() {
                    @Override
                    public void createDrops(BeforeBlockToItem event, Block block) {
                        event.removeBlockFromGeneration(block.getBlockFamily());
                        event.addBlockToGenerate(blockManager.getBlockFamily(new BlockUri("Core", "Dirt")), 1);
                        FastRandom rnd = new FastRandom();
                        if (rnd.nextFloat() < 0.5)
                            event.addItemToGenerate(entityManager.create("WoodAndStone:stone"));
                    }
                });
        blockDrops.put(new BlockUri("Core", "Dirt"),
                new Drop() {
                    @Override
                    public void createDrops(BeforeBlockToItem event, Block block) {
                        FastRandom rnd = new FastRandom();
                        if (rnd.nextFloat() < 0.5)
                            event.addItemToGenerate(entityManager.create("WoodAndStone:stone"));
                    }
                });
        blockDrops.put(new BlockUri("Core", "Stone"),
                new Drop() {
                    @Override
                    public void createDrops(BeforeBlockToItem event, Block block) {
                        event.removeBlockFromGeneration(block.getBlockFamily());
                        EntityRef entityRef = entityManager.create("WoodAndStone:stone");
                        ItemComponent item = entityRef.getComponent(ItemComponent.class);
                        item.stackCount = 3;
                        entityRef.saveComponent(item);
                        event.addItemToGenerate(entityRef);
                    }
                });
        blockDrops.put(new BlockUri("Core", "GreenLeaf"),
                new Drop() {
                    @Override
                    public void createDrops(BeforeBlockToItem event, Block block) {
                        event.removeBlockFromGeneration(block.getBlockFamily());
                        FastRandom rnd = new FastRandom();
                        if (rnd.nextFloat() < 0.3f)
                            event.addItemToGenerate(entityManager.create("WoodAndStone:stick"));
                        if (rnd.nextFloat() < 0.1f)
                            event.addBlockToGenerate(blockManager.getBlockFamily("Core:OakSapling"), 1);
                    }
                });
        blockDrops.put(new BlockUri("Core", "OakBranch"),
                new Drop() {
                    @Override
                    public void createDrops(BeforeBlockToItem event, Block block) {
                        event.removeBlockFromGeneration(block.getBlockFamily());
                        event.addItemToGenerate(entityManager.create("WoodAndStone:stick"));
                    }
                });
    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent
    public void whenBlockDropped(BeforeBlockToItem event, EntityRef blockEntity) {
        BlockComponent blockComponent = blockEntity.getComponent(BlockComponent.class);
        Block block = worldProvider.getBlock(blockComponent.getPosition());
        Drop blockDrop = blockDrops.get(block.getBlockFamily().getURI());
        if (blockDrop != null) {
            blockDrop.createDrops(event, block);
        }
    }

    private interface Drop {
        public void createDrops(BeforeBlockToItem event, Block block);
    }
}
