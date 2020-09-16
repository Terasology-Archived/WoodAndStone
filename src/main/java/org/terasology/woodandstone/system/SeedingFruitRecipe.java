// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.system;

import com.google.common.base.Predicate;
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.hand.CraftInHandIngredientPredicate;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.PlayerInventorySlotResolver;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.naming.Name;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemIcon;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SeedingFruitRecipe implements CraftInHandRecipe {
    private static final IngredientCraftBehaviour<EntityRef> KNIFE_BEHAVIOUR = new ReduceDurabilityCraftBehaviour(
            new CraftInHandIngredientPredicate("WoodAndStone:knife"), 1, PlayerInventorySlotResolver.singleton());
    private static final ConsumeFruitBehaviour FRUIT_BEHAVIOUR = new ConsumeFruitBehaviour();

    @Override
    public List<CraftInHandResult> getMatchingRecipeResults(EntityRef character) {
        String knifeSlot = getKnifeSlot(character);
        if (knifeSlot == null) {
            return null;
        }

        List<CraftInHandResult> results = new LinkedList<>();

        final List<String> fruitParameters = FRUIT_BEHAVIOUR.getValidToCraft(character, 1);
        for (String fruitParameter : fruitParameters) {
            results.add(new Result(Arrays.asList(knifeSlot, fruitParameter)));
        }

        return results;
    }

    private String getKnifeSlot(EntityRef character) {
        List<String> slots = KNIFE_BEHAVIOUR.getValidToCraft(character, 1);
        if (slots.size() > 0) {
            return slots.get(0);
        }
        return null;
    }

    @Override
    public CraftInHandResult getResultByParameters(List<String> parameters) {
        return new Result(parameters);
    }

    public static final class Result implements CraftInHandResult {
        private final List<String> parameters;
        private List<CraftIngredientRenderer> renderers;

        private Result(List<String> parameters) {
            this.parameters = parameters;
        }

        @Override
        public List<String> getParameters() {
            return parameters;
        }

        @Override
        public int getMaxMultiplier(EntityRef entity) {
            int maxMultiplier = KNIFE_BEHAVIOUR.getMaxMultiplier(entity, parameters.get(0));
            maxMultiplier = Math.min(maxMultiplier, FRUIT_BEHAVIOUR.getMaxMultiplier(entity, parameters.get(1)));
            return maxMultiplier;
        }

        @Override
        public EntityRef craft(EntityRef character, int count) {
            if (!isValidForCrafting(character, count)) {
                return EntityRef.NULL;
            }

            KNIFE_BEHAVIOUR.processIngredient(character, character, parameters.get(0), count);
            FRUIT_BEHAVIOUR.processIngredient(character, character, parameters.get(1), count);

            EntityRef result =
                    CoreRegistry.get(EntityManager.class).create(FRUIT_BEHAVIOUR.getSeedResult(parameters.get(1)));
            ItemComponent itemComponent = result.getComponent(ItemComponent.class);
            itemComponent.icon =
                    Assets.getTextureRegion("AnotherWorldPlants:SeedBag(" + FRUIT_BEHAVIOUR.getFruitIcon(parameters.get(1)) + ")").get();
            result.saveComponent(itemComponent);

            return result;
        }

        @Override
        public boolean isValidForCrafting(EntityRef entity, int multiplier) {
            if (!KNIFE_BEHAVIOUR.isValidToCraft(entity, parameters.get(0), multiplier)) {
                return false;
            }
            return FRUIT_BEHAVIOUR.isValidToCraft(entity, parameters.get(1), multiplier);
        }

        @Override
        public List<CraftIngredientRenderer> getIngredientRenderers(EntityRef entity) {
            if (renderers == null) {
                renderers = new LinkedList<>();
                renderers.add(FRUIT_BEHAVIOUR.getRenderer(entity, parameters.get(1)));
                renderers.add(KNIFE_BEHAVIOUR.getRenderer(entity, parameters.get(0)));
            }
            return renderers;
        }

        @Override
        public long getProcessDuration() {
            return 0;
        }

        @Override
        public int getResultQuantity() {
            return 1;
        }

        @Override
        public void setupResultDisplay(ItemIcon itemIcon) {
            Prefab prefab =
                    CoreRegistry.get(PrefabManager.class).getPrefab(FRUIT_BEHAVIOUR.getSeedResult(parameters.get(1)));

            itemIcon.setIcon(Assets.getTextureRegion("AnotherWorldPlants:SeedBag(" + FRUIT_BEHAVIOUR.getFruitIcon(parameters.get(1)) + ")").get());
            DisplayNameComponent displayName = prefab.getComponent(DisplayNameComponent.class);
            if (displayName != null) {
                itemIcon.setTooltip(displayName.name);
            }
        }
    }

    private static class ConsumeFruitBehaviour extends ConsumeItemCraftBehaviour {
        private static final Name PLANT_PACK_MODULE = new Name("anotherworldplants");

        public ConsumeFruitBehaviour() {
            super(new Predicate<EntityRef>() {
                @Override
                public boolean apply(EntityRef input) {
                    Prefab prefab = input.getParentPrefab();
                    return prefab != null && prefab.getUrn().getModuleName().equals(PLANT_PACK_MODULE)
                            && prefab.getUrn().getResourceName().toString().toLowerCase().endsWith("fruit");
                }
            }, 1, PlayerInventorySlotResolver.singleton());
        }

        @Override
        protected List<Integer> getSlots(String parameter) {
            return super.getSlots(parameter.substring(0, parameter.indexOf('|')));
        }

        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            Prefab prefab = item.getParentPrefab();
            String assetName = prefab.getUrn().getResourceName().toString();
            String fruitName = assetName.substring(0, assetName.length() - 5);

            ItemComponent component = item.getComponent(ItemComponent.class);
            return super.getParameter(slots, item) + "|" + fruitName + "|" + component.icon.getUrn().toString();
        }

        public String getSeedResult(String parameter) {
            String[] split = parameter.split("\\|");
            return "AnotherWorldPlants:" + split[1] + "Seed";
        }

        public String getFruitIcon(String parameter) {
            String[] split = parameter.split("\\|");
            return split[2];
        }
    }
}
