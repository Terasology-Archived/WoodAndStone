// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.component;

import org.terasology.engine.entitySystem.Component;

import java.util.List;

// Add this Component to any recipe prefab that is supposed to be creatable in a HerbalismStation or similar.
// Include in prefab along with CraftingStationRecipeComponent to work properly.
public class HerbalismStationRecipeComponent implements Component {
    // The following variables are unused.
    public String recipeId;
    public List<String> recipeComponents;
    public List<String> recipeTools;
    public List<String> recipeFluids;

    public float requiredTemperature;
    public long processingDuration;

    public String itemResult;
    public String blockResult;
}
