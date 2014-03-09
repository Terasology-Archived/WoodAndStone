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
package org.terasology.herbalism.system;

import com.google.common.base.Function;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.genome.GenomeDefinition;
import org.terasology.genome.GenomeRegistry;
import org.terasology.genome.breed.BreedingAlgorithm;
import org.terasology.genome.breed.SimpleBreedingAlgorithm;
import org.terasology.genome.breed.mutator.GeneMutator;
import org.terasology.genome.genomeMap.SeedBasedGenomeMap;
import org.terasology.herbalism.HerbEffect;
import org.terasology.herbalism.HerbEffectRegistry;
import org.terasology.herbalism.HerbGeneMutator;
import org.terasology.herbalism.HerbNameProvider;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.effect.DoNothingEffect;
import org.terasology.herbalism.effect.HealEffect;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HerbalismAuthoritySystem extends BaseComponentSystem {
    @In
    private GenomeRegistry genomeRegistry;
    @In
    private WorldProvider worldProvider;
    @In
    private HerbEffectRegistry herbEffectRegistry;

    @Override
    public void preBegin() {
        herbEffectRegistry.registerHerbEffect(1f, new DoNothingEffect());
        herbEffectRegistry.registerHerbEffect(1f, new HealEffect());

        final HerbNameProvider herbNameProvider = new HerbNameProvider(worldProvider.getSeed().hashCode());

        int genomeLength = 10;

        GeneMutator herbGeneMutator = new HerbGeneMutator();

        BreedingAlgorithm herbBreedingAlgorithm = new SimpleBreedingAlgorithm(genomeLength, 9, 0.005f, herbGeneMutator);

        SeedBasedGenomeMap herbGenomeMap = new SeedBasedGenomeMap(worldProvider.getSeed().hashCode());
        herbGenomeMap.addSeedBasedProperty(Herbalism.EFFECT_PROPERTY, genomeLength, 3, HerbEffect.class,
                new Function<String, HerbEffect>() {
                    @Override
                    public HerbEffect apply(String input) {
                        final int i = input.hashCode();
                        float value;
                        if (i < 0) {
                            value = 0.5f + 0.5f * i / Integer.MIN_VALUE;
                        } else {
                            value = 0.5f * i / Integer.MAX_VALUE;
                        }
                        return herbEffectRegistry.getHerbEffect(value);
                    }
                });
        herbGenomeMap.addSeedBasedProperty(Herbalism.DURATION_PROPERTY, genomeLength, 2, Long.class,
                new Function<String, Long>() {
                    @Override
                    public Long apply(String input) {
                        int multiplier = input.charAt(0) - 'A' + 1;
                        int duration = 1000 * (input.charAt(1) - 'A' + 1);
                        return (long) (duration * multiplier);
                    }
                });
        herbGenomeMap.addSeedBasedProperty(Herbalism.MAGNITUDE_PROPERTY, genomeLength, 2, Float.class,
                new Function<String, Float>() {
                    @Override
                    public Float apply(String input) {
                        int multiplier = input.charAt(0) - 'A' + 1;
                        float duration = 0.25f * 0.25f * (input.charAt(1) - 'A' + 1);
                        return duration * multiplier;
                    }
                });
        herbGenomeMap.addSeedBasedProperty(Herbalism.NAME_PROPERTY, genomeLength, genomeLength, String.class,
                new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return herbNameProvider.getName(input);
                    }
                });

        GenomeDefinition herbGenomeDefinition = new GenomeDefinition(herbBreedingAlgorithm, herbGenomeMap);

        genomeRegistry.registerType("Herbalism:herb", herbGenomeDefinition);
    }
}
