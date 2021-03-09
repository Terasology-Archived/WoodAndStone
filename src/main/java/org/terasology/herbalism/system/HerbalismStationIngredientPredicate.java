/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.herbalism.system;

import com.google.common.base.Predicate;
import org.terasology.crafting.component.CraftingStationIngredientComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.herbalism.component.HerbComponent;

public class HerbalismStationIngredientPredicate implements Predicate<EntityRef> {
    private String itemType;

    public HerbalismStationIngredientPredicate(String itemType) {
        this.itemType = itemType;
    }

    @Override
    public boolean apply(EntityRef input) {
        HerbComponent hComponent = input.getComponent(HerbComponent.class);
        CraftingStationIngredientComponent component = input.getComponent(CraftingStationIngredientComponent.class);

        // If this contains a valid instance of HerbComponent, CraftingStationIngredientComponent and the input's
        // type matches the itemType of this predicate.
        return hComponent != null && component != null && component.type.equalsIgnoreCase(itemType);
    }
}
