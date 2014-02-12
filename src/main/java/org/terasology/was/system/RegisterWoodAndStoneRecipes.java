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

import org.terasology.anotherWorld.util.Filter;
import org.terasology.crafting.component.CraftingStationMaterialComponent;
import org.terasology.crafting.system.CraftInHandRecipeRegistry;
import org.terasology.crafting.system.CraftingWorkstationProcess;
import org.terasology.crafting.system.CraftingWorkstationProcessFactory;
import org.terasology.crafting.system.recipe.hand.CompositeTypeBasedCraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.SimpleConsumingCraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.hand.behaviour.DoNothingCraftBehaviour;
import org.terasology.crafting.system.recipe.hand.behaviour.ReduceItemDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.workstation.SimpleWorkstationRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.multiBlock.BasicHorizontalSizeFilter;
import org.terasology.multiBlock.MultiBlockFormRecipeRegistry;
import org.terasology.multiBlock.UniformBlockReplacementCallback;
import org.terasology.multiBlock.recipe.UniformMultiBlockFormItemRecipe;
import org.terasology.registry.In;
import org.terasology.was.WoodAndStone;
import org.terasology.workstation.system.WorkstationRegistry;
import org.terasology.world.block.BlockManager;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class RegisterWoodAndStoneRecipes implements ComponentSystem {
    @In
    private CraftInHandRecipeRegistry recipeRegistry;
    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private MultiBlockFormRecipeRegistry multiBlockFormRecipeRegistry;
    @In
    private BlockManager blockManager;

    @Override
    public void initialise() {
        workstationRegistry.registerProcessFactory(WoodAndStone.BASIC_WOODCRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());
        workstationRegistry.registerProcessFactory(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        workstationRegistry.registerProcessFactory(WoodAndStone.BASIC_STONECRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());
        workstationRegistry.registerProcessFactory(WoodAndStone.ADVANCED_STONECRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        workstationRegistry.registerProcessFactory(WoodAndStone.HEARTH_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        addWorkstationRecipes();

        addCraftInHandRecipes();

        addStandardWorkstationRecipes();

        addStoneWorkstationRecipes();
    }

    private void addWorkstationRecipes() {
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(new ToolTypeEntityFilter("axe"), new UseOnTopFilter(),
                        new StationTypeFilter("WoodAndStone:BasicWoodcrafting"), new BasicHorizontalSizeFilter(2, 1, 1, 1),
                        "WoodAndStone:BasicWoodcrafting",
                        new UniformBlockReplacementCallback<Void>(blockManager.getBlock("WoodAndStone:BasicWoodStation"))));
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(new ToolTypeEntityFilter("hammer"), new UseOnTopFilter(),
                        new StationTypeFilter("WoodAndStone:BasicStonecrafting"), new BasicHorizontalSizeFilter(2, 1, 1, 1),
                        "WoodAndStone:BasicStonecrafting",
                        new UniformBlockReplacementCallback<Void>(blockManager.getBlock("WoodAndStone:BasicStoneStation"))));
    }

    private void addWorkstationBlockShapesRecipe(String processType, String workstationType, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                                 String tool, int toolDurability, String blockResultPrefix, int blockResultCount) {
        SimpleWorkstationRecipe fullBlockRecipe = new SimpleWorkstationRecipe();
        fullBlockRecipe.addIngredient(ingredient, ingredientBasicCount);
        fullBlockRecipe.addRequiredTool(tool, toolDurability);
        fullBlockRecipe.setBlockResult(blockResultPrefix, (byte) blockResultCount);

        workstationRegistry.registerProcess(processType, new CraftingWorkstationProcess(workstationType, recipeNamePrefix, fullBlockRecipe));

        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Stair", 3, 4, 2);

        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Slope", 1, 2, 2);
        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "UpperHalfSlope", 1, 2, 2);
        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "SlopeCorner", 1, 2, 2);

        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "SteepSlope", 1, 1, 2);
        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "QuarterSlope", 1, 8, 2);

        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfBlock", 1, 2, 1);
        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfSlope", 1, 4, 2);
        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfSlopeCorner", 1, 6, 1);

        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "PillarTop", 1, 1, 2);
        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Pillar", 1, 1, 2);
        addShapeRecipe(processType, workstationType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "PillarBase", 1, 1, 2);
    }

    private void addShapeRecipe(String processType, String workstationType, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                String tool, int toolDurability, String blockResultPrefix, int blockResultCount,
                                String shape, int ingredientMultiplier, int resultMultiplier, int toolDurabilityMultiplier) {
        SimpleWorkstationRecipe shapeRecipe = new SimpleWorkstationRecipe();
        shapeRecipe.addIngredient(ingredient, ingredientBasicCount * ingredientMultiplier);
        shapeRecipe.addRequiredTool(tool, toolDurability * toolDurabilityMultiplier);
        shapeRecipe.setBlockResult(blockResultPrefix + ":Engine:" + shape, (byte) (blockResultCount * resultMultiplier));

        workstationRegistry.registerProcess(processType, new CraftingWorkstationProcess(workstationType, recipeNamePrefix + shape, shapeRecipe));
    }

    private void addStoneWorkstationRecipes() {
        addWorkstationBlockShapesRecipe(WoodAndStone.BASIC_STONECRAFTING_PROCESS_TYPE, "WoodAndStone:BasicStonecrafting", "Building|Cobble Stone|WoodAndStone:CobbleBlock",
                "WoodAndStone:stone", 2, "hammer", 1, "Core:CobbleStone", 1);
        addWorkstationBlockShapesRecipe(WoodAndStone.ADVANCED_STONECRAFTING_PROCESS_TYPE, "WoodAndStone:StandardStonecrafting", "Building|Bricks|WoodAndStone:BrickBlock",
                "WoodAndStone:brick", 2, "hammer", 1, "Core:Brick", 1);
    }

    private void addCraftInHandRecipes() {
        addCraftInHandRecipe("WoodAndStone:CrudeHammer",
                new SimpleConsumingCraftInHandRecipe("WoodAndStone:stick", "WoodAndStone:twig", "WoodAndStone:stone", "WoodAndStone:CrudeHammer"));
        addCraftInHandRecipe("WoodAndStone:CrudeAxe",
                new SimpleConsumingCraftInHandRecipe("WoodAndStone:stick", "WoodAndStone:twig", "WoodAndStone:sharpStone", "WoodAndStone:CrudeAxe"));

        addCraftInHandRecipe("WoodAndStone:sharpStone",
                new CompositeTypeBasedCraftInHandRecipe(
                        "WoodAndStone:stone", new ConsumeItemCraftBehaviour("WoodAndStone:stone"),
                        "WoodAndStone:hammer", new ReduceItemDurabilityCraftBehaviour("WoodAndStone:hammer", 1),
                        null, new DoNothingCraftBehaviour(),
                        "WoodAndStone:sharpStone"));

        addCraftInHandRecipe("WoodAndStone:unlitTorch",
                new CompositeTypeBasedCraftInHandRecipe(
                        "WoodAndStone:stick", new ConsumeItemCraftBehaviour("WoodAndStone:stick"),
                        "WoodAndStone:resin", new ReduceItemDurabilityCraftBehaviour("WoodAndStone:resin", 1),
                        null, new DoNothingCraftBehaviour(),
                        "WoodAndStone:UnlitTorch"));

        addCraftInHandRecipe("WoodAndStone:litTorch",
                new CompositeTypeBasedCraftInHandRecipe(
                        "WoodAndStone:unlitTorch", new ConsumeItemCraftBehaviour("WoodAndStone:unlitTorch"),
                        "WoodAndStone:flint", new ReduceItemDurabilityCraftBehaviour("WoodAndStone:flint", 1),
                        null, new DoNothingCraftBehaviour(),
                        "WoodAndStone:LitTorch", true));

        addCraftInHandRecipe("WoodAndStone:clayHearth",
                new CompositeTypeBasedCraftInHandRecipe(
                        "WoodAndStone:clay", new ConsumeItemCraftBehaviour("WoodAndStone:clay", 9),
                        null, new DoNothingCraftBehaviour(),
                        null, new DoNothingCraftBehaviour(),
                        "WoodAndStone:ClayHearth", true));
    }

    private void addStandardWorkstationRecipes() {
        addWorkstationBlockShapesRecipe(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, "WoodAndStone:StandardWoodcrafting", "Building|Planks|WoodAndStone:PlankBlock",
                "WoodAndStone:plank", 2, "axe", 1, "Core:Plank", 4);
        addWorkstationBlockShapesRecipe(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, "WoodAndStone:StandardWoodcrafting", "Building|Fine Planks|WoodAndStone:FinePlankBlock",
                "WoodAndStone:plank", 4, "hammer", 1, "WoodAndStone:FinePlank", 1);
    }

    public void addCraftInHandRecipe(String recipeId, CraftInHandRecipe craftInHandRecipe) {
        recipeRegistry.addCraftInHandRecipe(recipeId, craftInHandRecipe);
    }

    @Override
    public void shutdown() {
    }

    private final class StationTypeFilter implements Filter<EntityRef> {
        private String stationType;

        private StationTypeFilter(String stationType) {
            this.stationType = stationType;
        }

        @Override
        public boolean accepts(EntityRef entity) {
            CraftingStationMaterialComponent stationMaterial = entity.getComponent(CraftingStationMaterialComponent.class);
            return stationMaterial != null && stationMaterial.stationType.equals(stationType);
        }
    }
}
