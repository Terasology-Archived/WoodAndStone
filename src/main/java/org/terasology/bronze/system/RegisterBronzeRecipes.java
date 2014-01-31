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

            // Edges of top layer will be different
            Map<Vector3i, Block> result = new HashMap<>();
            for (Vector3i vector3i : region) {
                if (vector3i.y != max.y
                        || (vector3i.x > min.x && vector3i.x < max.x && vector3i.z > min.z && vector3i.z < max.z)) {
                    result.put(vector3i, cobbleStone);
                } else {
                    if (vector3i.x == min.x && vector3i.z == min.z) {
                        result.put(vector3i, blockManager.getBlock("Core:CobbleStone:Engine:SlopeCorner.LEFT"));
                    } else if (vector3i.x == max.x && vector3i.z == max.z) {
                        result.put(vector3i, blockManager.getBlock("Core:CobbleStone:Engine:SlopeCorner.RIGHT"));
                    } else if (vector3i.x == min.x && vector3i.z == max.z) {
                        result.put(vector3i, blockManager.getBlock("Core:CobbleStone:Engine:SlopeCorner.BACK"));
                    } else if (vector3i.x == max.x && vector3i.z == min.z) {
                        result.put(vector3i, blockManager.getBlock("Core:CobbleStone:Engine:SlopeCorner.FRONT"));
                    } else if (vector3i.x == min.x) {
                        result.put(vector3i, blockManager.getBlock("Core:CobbleStone:Engine:Slope.LEFT"));
                    } else if (vector3i.x == max.x) {
                        result.put(vector3i, blockManager.getBlock("Core:CobbleStone:Engine:Slope.RIGHT"));
                    } else if (vector3i.z == min.z) {
                        result.put(vector3i, blockManager.getBlock("Core:CobbleStone:Engine:Slope.FRONT"));
                    } else if (vector3i.z == max.z) {
                        result.put(vector3i, blockManager.getBlock("Core:CobbleStone:Engine:Slope.BACK"));
                    } else {
                        result.put(vector3i, BlockManager.getAir());
                    }
                }
            }

            return result;
        }

        @Override
        public void multiBlockFormed(EntityRef entity, Region3i region) {
        }
    }

    private final static class AllowableCharcoalPitSize implements Filter<Vector3i> {
        @Override
        public boolean accepts(Vector3i value) {
            if (value.x < 3 || value.y < 3 || value.z < 3) {
                return false;
            }
            return true;
        }
    }
}
