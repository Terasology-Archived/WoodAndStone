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
import org.terasology.was.system.CraftToolsInHandAuthoritySystem;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleCraftInHandRecipe implements CraftToolsInHandAuthoritySystem.CraftInHandRecipe {
    private String handleType;
    private String toolHeadType;
    private String resultPrefab;

    public SimpleCraftInHandRecipe(String handleType, String toolHeadType, String resultPrefab) {
        this.handleType = handleType;
        this.toolHeadType = toolHeadType;
        this.resultPrefab = resultPrefab;
    }

    @Override
    public boolean matchesRecipe(CraftInHandRecipeComponent handle, CraftInHandRecipeComponent toolHead) {
        return handle.componentType.equals(this.handleType) && toolHead.componentType.equals(this.toolHeadType);
    }

    @Override
    public String getResultPrefab() {
        return resultPrefab;
    }
}
