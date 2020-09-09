// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.component;

import org.terasology.engine.entitySystem.Component;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationUpgradeRecipeComponent implements Component {
    public static final String PROCESS_TYPE = "Crafting:UpgradeCraftingWorkstation";

    public String stationType;
    public String targetStationType;
    public String targetStationPrefab;
    public List<String> recipeComponents;
    public String resultBlockUri;
}
