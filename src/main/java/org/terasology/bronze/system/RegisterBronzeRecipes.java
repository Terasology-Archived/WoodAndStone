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
package org.terasology.bronze.system;

import org.terasology.anotherWorld.util.Filter;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.multiBlock.MultiBlockFormRecipeRegistry;
import org.terasology.multiBlock.SurroundMultiBlockFormItemRecipe;
import org.terasology.multiBlock.UniformMultiBlockFormItemRecipe;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.was.system.ToolTypeEntityFilter;
import org.terasology.was.system.UseOnTopFilter;
import org.terasology.workstation.system.CraftingStationRecipeRegistry;
import org.terasology.workstation.system.recipe.SimpleWorkstationRecipe;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class RegisterBronzeRecipes implements ComponentSystem {
    @In
    private CraftingStationRecipeRegistry craftingStationRecipeRegistry;
    @In
    private MultiBlockFormRecipeRegistry multiBlockRecipeRegistry;

    @Override
    public void initialise() {
        addWorkstationRecipes();

        addMultiblockRecipes();

        addBasicMetalcraftingRecipes();
    }

    private void addBasicMetalcraftingRecipes() {
        SimpleWorkstationRecipe chalcopyriteDustRecipe = new SimpleWorkstationRecipe();
        chalcopyriteDustRecipe.addIngredient("WoodAndStone:copperOre", 1);
        chalcopyriteDustRecipe.addRequiredTool("stone", 1);
        chalcopyriteDustRecipe.setItemResult("WoodAndStone:CopperOreDust", (byte) 1);

        craftingStationRecipeRegistry.addCraftingStationRecipe(
                "WoodAndStone:BasicMetalcrafting", "WoodAndStone:ChalcopyriteDust", chalcopyriteDustRecipe);
    }

    private void addWorkstationRecipes() {
        multiBlockRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(
                        new ToolTypeEntityFilter("stone"), new UseOnTopFilter(),
                        new BlockUriEntityFilter(new BlockUri("Core", "CobbleStone")), new Vector3i(2, 1, 1),
                        "WoodAndStone:BasicMetalcrafting", "WoodAndStone:BasicMetalStation"));
    }

    private void addMultiblockRecipes() {
        multiBlockRecipeRegistry.addMultiBlockFormItemRecipe(
                new SurroundMultiBlockFormItemRecipe(
                        new ToolTypeEntityFilter("stone"), new BlockUriEntityFilter(new BlockUri("Core", "CobbleStone")),
                        new BlockUriEntityFilter(new BlockUri("Engine", "Air")), new AllowableCharcoalPitSize(),
                        new Filter<ActivateEvent>() {
                            @Override
                            public boolean accepts(ActivateEvent value) {
                                return true;
                            }
                        }, "WoodAndStone:CharcoalPit", new CharcoalPitCallback())
        );
    }

    @Override
    public void shutdown() {
    }

    private final static class CharcoalPitCallback implements SurroundMultiBlockFormItemRecipe.Callback {
        @Override
        public Map<Vector3i, Block> createReplacementBlockMap(Region3i region) {
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            Block cobbleStone = blockManager.getBlock("Core:CobbleStone");

            Vector3i min = region.min();
            Vector3i max = region.max();
            Vector3i size = region.size();
            Vector3f center = region.center();

            // Generate map of blocks
            Map<Vector3i, Block> result = new HashMap<>();

            // Fill up the non-top layer blocks
            Region3i nonTopLayer = Region3i.createFromMinAndSize(min, new Vector3i(size.x, size.y - 1, size.z));
            for (Vector3i position : nonTopLayer) {
                result.put(position, cobbleStone);
            }

            // Fill up the internal blocks of top layer
            Block halfBlock = blockManager.getBlock("Core:CobbleStone:Engine:HalfBlock");
            Region3i topLayerInternal = Region3i.createFromMinAndSize(new Vector3i(min.x, max.y, min.z), new Vector3i(size.x, 1, size.z));
            for (Vector3i position : topLayerInternal) {
                result.put(position, halfBlock);
            }

            // Top layer sides
            for (int x = min.x + 1; x < max.x; x++) {
                result.put(new Vector3i(x, max.y, min.z), blockManager.getBlock("Core:CobbleStone:Engine:HalfSlope.FRONT"));
                result.put(new Vector3i(x, max.y, max.z), blockManager.getBlock("Core:CobbleStone:Engine:HalfSlope.BACK"));
            }
            for (int z = min.z + 1; z < max.z; z++) {
                result.put(new Vector3i(min.x, max.y, z), blockManager.getBlock("Core:CobbleStone:Engine:HalfSlope.LEFT"));
                result.put(new Vector3i(max.x, max.y, z), blockManager.getBlock("Core:CobbleStone:Engine:HalfSlope.RIGHT"));
            }

            // Top layer corners
            result.put(new Vector3i(min.x, max.y, min.z), blockManager.getBlock("Core:CobbleStone:Engine:HalfSlopeCorner.LEFT"));
            result.put(new Vector3i(max.x, max.y, max.z), blockManager.getBlock("Core:CobbleStone:Engine:HalfSlopeCorner.RIGHT"));
            result.put(new Vector3i(min.x, max.y, max.z), blockManager.getBlock("Core:CobbleStone:Engine:HalfSlopeCorner.BACK"));
            result.put(new Vector3i(max.x, max.y, min.z), blockManager.getBlock("Core:CobbleStone:Engine:HalfSlopeCorner.FRONT"));

            // Chimney
            result.put(new Vector3i(center.x, max.y, center.z), blockManager.getBlock("Core:CobbleStone:Engine:PillarBase"));

            return result;
        }

        @Override
        public void multiBlockFormed(EntityRef entity, Region3i region) {
        }
    }

    private final static class AllowableCharcoalPitSize implements Filter<Vector3i> {
        @Override
        public boolean accepts(Vector3i value) {
            // Minimum size 3x3x3
            return (value.x >= 3 && value.y >= 3 && value.z >= 3
                    // X and Z are odd to allow finding center block
                    && value.x % 2 == 1 && value.z % 2 == 1);
        }
    }
}
