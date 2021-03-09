/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.herbalism;

import org.terasology.engine.utilities.Assets;
import org.terasology.namegenerator.data.NameGeneratorComponent;
import org.terasology.namegenerator.generators.MarkovNameGenerator;

import java.util.List;

public class HerbNameProvider {
    private final MarkovNameGenerator generaGen;
    private final MarkovNameGenerator familyGen;

    public HerbNameProvider(int seed) {
        final List<String> families = Assets.getPrefab("NameGenerator:floweringPlantsFamilies").get().getComponent(NameGeneratorComponent.class).nameList;
        final List<String> generas = Assets.getPrefab("NameGenerator:floweringPlantsGenera").get().getComponent(NameGeneratorComponent.class).nameList;

        generaGen = new MarkovNameGenerator(seed, generas);
        familyGen = new MarkovNameGenerator(seed + 937623, families);
    }

    public String getName(String seed) {
        int length = seed.length();
        return generaGen.getName(4, 8, seed.substring(0, length / 2).hashCode()) + " " + familyGen.getName(4, 8, seed.substring(0, length).hashCode());
    }
}
