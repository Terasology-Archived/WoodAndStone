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
package org.terasology.was.system.recipe.hand;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.was.component.CraftInHandRecipeComponent;
import org.terasology.was.system.recipe.hand.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.was.system.recipe.hand.behaviour.DoNothingCraftBehaviour;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CompositeTypeBasedCraftInHandRecipe implements CraftInHandRecipe {
    private String item1Type;
    private String item2Type;
    private String item3Type;
    private Map<String, ItemCraftBehaviour> itemCraftBehaviourMap;
    private String resultPrefab;

    public CompositeTypeBasedCraftInHandRecipe(String item1Type, String item2Type, String item3Type, Map<String, ItemCraftBehaviour> itemCraftBehaviourMap, String resultPrefab) {
        this.item1Type = item1Type;
        this.item2Type = item2Type;
        this.item3Type = item3Type;
        this.itemCraftBehaviourMap = itemCraftBehaviourMap;
        this.resultPrefab = resultPrefab;
    }

    @Override
    public CraftInHandResult getMatchingRecipeResult(CraftInHandRecipeComponent item1Type, CraftInHandRecipeComponent item2Type, CraftInHandRecipeComponent item3Type) {
        if (compareItems(item1Type, this.item1Type) && compareItems(item2Type, this.item2Type) && compareItems(item3Type, this.item3Type)) {
            return new CraftResult();
        }
        return null;
    }

    private boolean compareItems(CraftInHandRecipeComponent itemType, String storedType) {
        if (itemType == null && storedType == null)
            return true;
        if (itemType == null || storedType == null)
            return false;
        return itemType.componentType.equals(storedType);
    }

    public class CraftResult implements CraftInHandResult {
        @Override
        public String getResultPrefab() {
            return resultPrefab;
        }

        @Override
        public boolean processCraftingForCharacter(EntityRef character, EntityRef item1, EntityRef item2, EntityRef item3) {
            ItemCraftBehaviour behaviour1 = getCraftBehaviourForItemType(item1Type);
            ItemCraftBehaviour behaviour2 = getCraftBehaviourForItemType(item2Type);
            ItemCraftBehaviour behaviour3 = getCraftBehaviourForItemType(item3Type);

            if (behaviour1.isValid(character, item1)
                    && behaviour2.isValid(character, item2)
                    && behaviour3.isValid(character, item3)) {
                behaviour1.processForItem(character, item1);
                behaviour2.processForItem(character, item2);
                behaviour3.processForItem(character, item3);

                return true;
            }

            return false;
        }

        private ItemCraftBehaviour getCraftBehaviourForItemType(String itemType) {
            if (itemType == null)
                return new DoNothingCraftBehaviour();
            ItemCraftBehaviour craftBehaviour = itemCraftBehaviourMap.get(itemType);
            if (craftBehaviour == null)
                craftBehaviour = new ConsumeItemCraftBehaviour();
            return craftBehaviour;
        }
    }
}
