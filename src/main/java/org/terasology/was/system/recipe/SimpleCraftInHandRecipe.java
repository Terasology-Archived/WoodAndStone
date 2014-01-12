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
package org.terasology.was.system.recipe;

import org.terasology.was.component.CraftInHandRecipeComponent;
import org.terasology.was.system.CraftInHandAuthoritySystem;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleCraftInHandRecipe implements CraftInHandAuthoritySystem.CraftInHandRecipe {
    private String item1Type;
    private String item2Type;
    private String item3Type;
    private String resultPrefab;

    public SimpleCraftInHandRecipe(String item1Type, String item2Type, String item3Type, String resultPrefab) {
        this.item1Type = item1Type;
        this.item2Type = item2Type;
        this.item3Type = item3Type;
        this.resultPrefab = resultPrefab;
    }

    @Override
    public boolean matchesRecipe(CraftInHandRecipeComponent item1Type, CraftInHandRecipeComponent item2Type, CraftInHandRecipeComponent item3Type) {
        return item1Type.componentType.equals(this.item1Type) && item2Type.componentType.equals(this.item2Type) && item3Type.componentType.equals(this.item3Type);
    }

    @Override
    public String getResultPrefab() {
        return resultPrefab;
    }
}
