// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism;

import org.terasology.engine.utilities.Assets;
import org.terasology.namegenerator.data.NameGeneratorComponent;
import org.terasology.namegenerator.generators.MarkovNameGenerator;

import java.util.List;

public class HerbNameProvider {
    private final MarkovNameGenerator generaGen;
    private final MarkovNameGenerator familyGen;

    public HerbNameProvider(int seed) {
        final List<String> families =
                Assets.getPrefab("NameGenerator:floweringPlantsFamilies").get().getComponent(NameGeneratorComponent.class).nameList;
        final List<String> generas =
                Assets.getPrefab("NameGenerator:floweringPlantsGenera").get().getComponent(NameGeneratorComponent.class).nameList;

        generaGen = new MarkovNameGenerator(seed, generas);
        familyGen = new MarkovNameGenerator(seed + 937623, families);
    }

    public String getName(String seed) {
        int length = seed.length();
        return generaGen.getName(4, 8, seed.substring(0, length / 2).hashCode()) + " " + familyGen.getName(4, 8,
                seed.substring(0, length).hashCode());
    }
}
