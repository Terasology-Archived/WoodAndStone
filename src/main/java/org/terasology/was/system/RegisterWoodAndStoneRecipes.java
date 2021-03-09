/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.was.system;

import com.google.common.base.Predicate;
import org.terasology.assets.ResourceUrn;
import org.terasology.crafting.component.CraftInHandRecipeComponent;
import org.terasology.crafting.component.CraftingStationMaterialComponent;
import org.terasology.crafting.component.CraftingStationRecipeComponent;
import org.terasology.crafting.system.CraftInHandRecipeRegistry;
import org.terasology.crafting.system.CraftingWorkstationProcess;
import org.terasology.crafting.system.CraftingWorkstationProcessFactory;
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.PresenceItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.hand.CompositeTypeBasedCraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.CraftInHandIngredientPredicate;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.PlayerInventorySlotResolver;
import org.terasology.crafting.system.recipe.render.RecipeResultFactory;
import org.terasology.crafting.system.recipe.render.result.BlockRecipeResultFactory;
import org.terasology.crafting.system.recipe.render.result.ItemRecipeResultFactory;
import org.terasology.crafting.system.recipe.workstation.DefaultWorkstationRecipe;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.herbalism.component.HerbalismStationRecipeComponent;
import org.terasology.multiBlock.Basic2DSizeFilter;
import org.terasology.multiBlock.Basic3DSizeFilter;
import org.terasology.multiBlock.BlockUriEntityFilter;
import org.terasology.multiBlock.MultiBlockFormRecipeRegistry;
import org.terasology.multiBlock.UniformBlockReplacementCallback;
import org.terasology.multiBlock.recipe.LayeredMultiBlockFormItemRecipe;
import org.terasology.multiBlock.recipe.UniformMultiBlockFormItemRecipe;
import org.terasology.was.WoodAndStone;
import org.terasology.was.ui.HerbalismCraftingStationRecipe;
import org.terasology.workstation.system.WorkstationRegistry;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class RegisterWoodAndStoneRecipes extends BaseComponentSystem {
    @In
    private CraftInHandRecipeRegistry recipeRegistry;
    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private MultiBlockFormRecipeRegistry multiBlockFormRecipeRegistry;
    @In
    private BlockManager blockManager;
    @In
    private PrefabManager prefabManager;

    @Override
    public void initialise() {
        workstationRegistry.registerProcessFactory(WoodAndStone.BASIC_WOODCRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());
        workstationRegistry.registerProcessFactory(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        workstationRegistry.registerProcessFactory(WoodAndStone.BASIC_STONECRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());
        workstationRegistry.registerProcessFactory(WoodAndStone.ADVANCED_STONECRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        workstationRegistry.registerProcessFactory(WoodAndStone.COOKING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());
        workstationRegistry.registerProcessFactory(WoodAndStone.HERBALISM_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        addWorkstationFormingRecipes();

        addCraftInHandRecipes();

        addHerbalismWorkstationRecipes();

        addWoodPlankRecipes();

        addStandardWoodWorkstationBlockShapeRecipes();

        addBasicStoneWorkstationBlockShapeRecipes();
    }

    private void addWoodPlankRecipes() {
        workstationRegistry.registerProcess(WoodAndStone.BASIC_WOODCRAFTING_PROCESS_TYPE,
                new CraftingWorkstationProcess(WoodAndStone.BASIC_WOODCRAFTING_PROCESS_TYPE, "Materials|WoodAndStone:WoodPlank",
                        new PlankRecipe(2)));

        workstationRegistry.registerProcess(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE,
                new CraftingWorkstationProcess(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, "Materials|WoodAndStone:WoodPlank",
                        new PlankRecipe(3)));
    }

    private void addWorkstationFormingRecipes() {
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(new ToolTypeEntityFilter("axe"), new UseOnTopFilter(),
                        new StationTypeFilter("WoodAndStone:BasicWoodcrafting"), new Basic3DSizeFilter(2, 1, 1, 1),
                        "WoodAndStone:BasicWoodcrafting",
                        new UniformBlockReplacementCallback<Void>(blockManager.getBlock("WoodAndStone:BasicWoodStation"))));
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(new ToolTypeEntityFilter("hammer"), new UseOnTopFilter(),
                        new StationTypeFilter("WoodAndStone:BasicStonecrafting"), new Basic3DSizeFilter(2, 1, 1, 1),
                        "WoodAndStone:BasicStonecrafting",
                        new UniformBlockReplacementCallback<Void>(blockManager.getBlock("WoodAndStone:BasicStoneStation"))));

        LayeredMultiBlockFormItemRecipe cookingStationRecipe = new LayeredMultiBlockFormItemRecipe(
                new ToolTypeEntityFilter("hammer"), new Basic2DSizeFilter(2, 1), new AnyActivityFilter(),
                "WoodAndStone:CookingStation", null);
        cookingStationRecipe.addLayer(1, 1, new BlockUriEntityFilter(new BlockUri("CoreAssets:Brick")));
        cookingStationRecipe.addLayer(1, 1, new BlockUriEntityFilter(new BlockUri(new ResourceUrn("CoreAssets:CobbleStone"), new ResourceUrn(("Engine:EighthBlock")))));
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(cookingStationRecipe);

        LayeredMultiBlockFormItemRecipe herbalismStationRecipe = new LayeredMultiBlockFormItemRecipe(
                new ToolTypeEntityFilter("hammer"), new Basic2DSizeFilter(3, 1), new AnyActivityFilter(),
                "WoodAndStone:HerbalismStation", null);
        herbalismStationRecipe.addLayer(1, 1, new BlockUriEntityFilter(new BlockUri("CoreAssets:Brick")));
        herbalismStationRecipe.addLayer(1, 1, new BlockUriEntityFilter(new BlockUri(new ResourceUrn("CoreAssets:CobbleStone"), new ResourceUrn(("Engine:EighthBlock")))));
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(herbalismStationRecipe);
    }

    // Add all of the recipes to the HerbalismStation.
    private void addHerbalismWorkstationRecipes() {
        // TODO: Temporarily removed for sake of testing.
        /* workstationRegistry.registerProcess(WoodAndStone.HERBALISM_PROCESS_TYPE,
                new CraftingWorkstationProcess(WoodAndStone.HERBALISM_PROCESS_TYPE, "WoodAndStone:HerbPotion", new HerbalismCraftingStationRecipe()));*/

        // Add all the recipes marked with "HerbalismStationRecipeComponent" in their prefabs and add them to the list.
        for (Prefab prefab : prefabManager.listPrefabs(HerbalismStationRecipeComponent.class)) {
            CraftingStationRecipeComponent recipeComponent = prefab.getComponent(CraftingStationRecipeComponent.class);

            workstationRegistry.registerProcess(WoodAndStone.HERBALISM_PROCESS_TYPE,
                    new CraftingWorkstationProcess(WoodAndStone.HERBALISM_PROCESS_TYPE, recipeComponent.recipeId,
                            new HerbalismCraftingStationRecipe(recipeComponent)));
        }
    }

    private void addStandardWoodWorkstationBlockShapeRecipes() {
        addPlankBlockRecipes();
        addWorkstationBlockShapesRecipe(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, "Building|Fine Planks|WoodAndStone:FinePlankBlock",
                "WoodAndStone:plank", 4, "hammer", 1, "WoodAndStone:FinePlank", 1);
    }

    private void addBasicStoneWorkstationBlockShapeRecipes() {
        addWorkstationBlockShapesRecipe(WoodAndStone.BASIC_STONECRAFTING_PROCESS_TYPE, "Building|Cobble Stone|WoodAndStone:CobbleBlock",
                "WoodAndStone:stone", 2, "hammer", 1, "CoreAssets:CobbleStone", 1);
        addWorkstationBlockShapesRecipe(WoodAndStone.ADVANCED_STONECRAFTING_PROCESS_TYPE, "Building|Bricks|WoodAndStone:BrickBlock",
                "WoodAndStone:brick", 2, "hammer", 1, "CoreAssets:Brick", 1);
    }

    private void addPlankBlockShapeRecipe(String shape, int ingredientMultiplier, int durabilityMultiplier, int resultMultiplier) {
        addPlankBlockShapeRecipe(shape, "Engine", ingredientMultiplier, durabilityMultiplier, resultMultiplier);
    }

    private void addPlankBlockShapeRecipe(String shape, String module, int ingredientMultiplier, int durabilityMultiplier, int resultMultiplier) {
        String recipeName = "Building|Planks|WoodAndStone:PlankBlock";
        if (shape != null) {
            recipeName += shape;
        }

        String resultShape = null;
        if (shape != null) {
            resultShape = module + ":" + shape;
        }

        workstationRegistry.registerProcess(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE,
                new CraftingWorkstationProcess(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, recipeName,
                        new PlankBlockRecipe(2 * ingredientMultiplier, durabilityMultiplier, resultShape, 4 * resultMultiplier)));
    }

    private void addPlankBlockRecipes() {
        addPlankBlockShapeRecipe(null, 1, 1, 1);
        addPlankBlockShapeRecipe("Stair", 3, 4, 2);

        addPlankBlockShapeRecipe("Slope", 1, 2, 2);
        addPlankBlockShapeRecipe("UpperHalfSlope", 1, 2, 2);
        addPlankBlockShapeRecipe("SlopeCorner", 1, 2, 2);

        addPlankBlockShapeRecipe("SteepSlope", 1, 1, 2);
        addPlankBlockShapeRecipe("QuarterSlope", 1, 8, 2);

        addPlankBlockShapeRecipe("HalfBlock", 1, 2, 1);
        addPlankBlockShapeRecipe("EighthBlock", 1, 8, 1);
        addPlankBlockShapeRecipe("HalfSlope", 1, 4, 2);
        addPlankBlockShapeRecipe("HalfSlopeCorner", 1, 6, 1);

        addPlankBlockShapeRecipe("PillarTop", "StructuralResources", 1, 1, 2);
        addPlankBlockShapeRecipe("Pillar", "StructuralResources", 1, 1, 2);
        addPlankBlockShapeRecipe("PillarBase", "StructuralResources", 1, 1, 2);
    }

    private void addCraftInHandRecipes() {
        addCraftInHandRecipe("WoodAndStone:SeedingFruits", new SeedingFruitRecipe());
        addCraftInHandRecipe("WoodAndStone:SeedingHerbs", new SeedingHerbRecipe());

        for (Prefab prefab : prefabManager.listPrefabs(CraftInHandRecipeComponent.class)) {
            parseCraftInHandRecipe(prefab.getComponent(CraftInHandRecipeComponent.class));
        }
    }

    private void addShapeRecipe(String processType, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                String tool, int toolDurability, String blockResultPrefix, int blockResultCount,
                                String shape, int ingredientMultiplier, int resultMultiplier, int toolDurabilityMultiplier) {
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount, shape,
                "engine", ingredientMultiplier, resultMultiplier, toolDurabilityMultiplier);
    }

    private void addShapeRecipe(String processType, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                String tool, int toolDurability, String blockResultPrefix, int blockResultCount,
                                String shape, String module, int ingredientMultiplier, int resultMultiplier, int toolDurabilityMultiplier) {
        DefaultWorkstationRecipe shapeRecipe = new DefaultWorkstationRecipe();
        shapeRecipe.addIngredient(ingredient, ingredientBasicCount * ingredientMultiplier);
        shapeRecipe.addRequiredTool(tool, toolDurability * toolDurabilityMultiplier);
        shapeRecipe.setResultFactory(new BlockRecipeResultFactory(blockManager.getBlockFamily(blockResultPrefix + ":" + module + ":" + shape).getArchetypeBlock(),
                blockResultCount * resultMultiplier));

        workstationRegistry.registerProcess(processType, new CraftingWorkstationProcess(processType, recipeNamePrefix + shape, shapeRecipe));
    }

    private void addWorkstationBlockShapesRecipe(String processType, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                                 String tool, int toolDurability, String blockResultPrefix, int blockResultCount) {
        DefaultWorkstationRecipe fullBlockRecipe = new DefaultWorkstationRecipe();
        fullBlockRecipe.addIngredient(ingredient, ingredientBasicCount);
        fullBlockRecipe.addRequiredTool(tool, toolDurability);
        fullBlockRecipe.setResultFactory(new BlockRecipeResultFactory(blockManager.getBlockFamily(blockResultPrefix).getArchetypeBlock(), blockResultCount));

        workstationRegistry.registerProcess(processType, new CraftingWorkstationProcess(processType, recipeNamePrefix, fullBlockRecipe));

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Stair", 3, 4, 2);

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Slope", 1, 2, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "UpperHalfSlope", 1, 2, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "SlopeCorner", 1, 2, 2);

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "SteepSlope", 1, 1, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "QuarterSlope", 1, 8, 2);

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfBlock", 1, 2, 1);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "EighthBlock", 1, 8, 1);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfSlope", 1, 4, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfSlopeCorner", 1, 6, 1);

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "PillarTop", "structuralResources", 1, 1, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Pillar", "structuralResources", 1, 1, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "PillarBase", "structuralResources", 1, 1, 2);
    }

    private void parseCraftInHandRecipe(CraftInHandRecipeComponent recipeComponent) {
        String recipeId = recipeComponent.recipeId;
        RecipeResultFactory resultFactory;
        if (recipeComponent.blockResult != null) {
            resultFactory = new BlockRecipeResultFactory(blockManager.getBlockFamily(recipeComponent.blockResult).getArchetypeBlock(), 1);
        } else {
            resultFactory = new ItemRecipeResultFactory(prefabManager.getPrefab(recipeComponent.itemResult), 1);
        }
        CompositeTypeBasedCraftInHandRecipe recipe = new CompositeTypeBasedCraftInHandRecipe(resultFactory);

        if (recipeComponent.recipeComponents != null) {
            for (String component : recipeComponent.recipeComponents) {
                String[] split = component.split("\\*");
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                recipe.addItemCraftBehaviour(new ConsumeItemCraftBehaviour(new CraftInHandIngredientPredicate(type), count, PlayerInventorySlotResolver.singleton()));
            }
        }
        if (recipeComponent.recipeTools != null) {
            for (String tool : recipeComponent.recipeTools) {
                String[] split = tool.split("\\*");
                int durability = Integer.parseInt(split[0]);
                String type = split[1];
                recipe.addItemCraftBehaviour(new ReduceDurabilityCraftBehaviour(new CraftInHandIngredientPredicate(type), durability,
                        PlayerInventorySlotResolver.singleton()));
            }
        }
        if (recipeComponent.recipeActivators != null) {
            for (String activator : recipeComponent.recipeActivators) {
                String[] split = activator.split("\\*");
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                recipe.addItemCraftBehaviour(new PresenceItemCraftBehaviour(new CraftInHandIngredientPredicate(type), count, PlayerInventorySlotResolver.singleton()));
            }
        }
        addCraftInHandRecipe(recipeId, recipe);
    }

    private void addCraftInHandRecipe(String recipeId, CraftInHandRecipe craftInHandRecipe) {
        recipeRegistry.addCraftInHandRecipe(recipeId, craftInHandRecipe);
    }

    private final class StationTypeFilter implements Predicate<EntityRef> {
        private String stationType;

        private StationTypeFilter(String stationType) {
            this.stationType = stationType;
        }

        @Override
        public boolean apply(EntityRef entity) {
            CraftingStationMaterialComponent stationMaterial = entity.getComponent(CraftingStationMaterialComponent.class);
            return stationMaterial != null && stationMaterial.stationType.equals(stationType);
        }
    }
}
