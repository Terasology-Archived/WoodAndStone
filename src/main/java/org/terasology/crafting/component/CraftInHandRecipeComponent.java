// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.component;

import org.terasology.engine.entitySystem.Component;

import java.util.List;

public class CraftInHandRecipeComponent implements Component {
    public String recipeId;
    public List<String> recipeComponents;
    public List<String> recipeTools;
    public List<String> recipeActivators;

    public String itemResult;
    public String blockResult;
}
