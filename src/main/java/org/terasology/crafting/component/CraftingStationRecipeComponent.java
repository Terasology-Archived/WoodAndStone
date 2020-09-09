// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.component;

import org.terasology.engine.entitySystem.Component;

import java.util.List;

public class CraftingStationRecipeComponent implements Component {
    public String recipeId;
    public List<String> recipeComponents;
    public List<String> recipeTools;
    public List<String> recipeFluids;

    public float requiredTemperature;
    public long processingDuration;

    public String itemResult;
    public String blockResult;
}
