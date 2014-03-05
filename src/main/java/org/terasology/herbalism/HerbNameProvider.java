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

import org.terasology.asset.Assets;
import org.terasology.namegenerator.data.NameGeneratorComponent;
import org.terasology.namegenerator.generators.MarkovNameGenerator;
import org.terasology.namegenerator.generators.NameGenerator;
import org.terasology.namegenerator.generators.TrainingGenerator;

import java.util.List;

public class HerbNameProvider {
    private final NameGenerator generaGen;
    private final NameGenerator familyGen;

    public HerbNameProvider(int seed) {
        final List<String> families = Assets.getPrefab("NameGenerator:floweringPlantsFamilies").getComponent(NameGeneratorComponent.class).nameList;
        final List<String> generas = Assets.getPrefab("NameGenerator:floweringPlantsGenera").getComponent(NameGeneratorComponent.class).nameList;

        generaGen = new MarkovNameGenerator(seed, generas);
        familyGen = new MarkovNameGenerator(seed, families);
    }

    public String getName(int seed) {
        return generaGen.nextName()+" "+familyGen.nextName();
    }
}
