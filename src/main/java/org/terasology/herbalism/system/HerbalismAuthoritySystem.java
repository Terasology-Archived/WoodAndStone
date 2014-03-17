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
import org.terasology.alterationEffects.breath.WaterBreathingAlterationEffect;
import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.speed.SwimSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.WalkSpeedAlterationEffect;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.farm.component.FarmSoilComponent;
import org.terasology.farm.event.SeedPlanted;
import org.terasology.genome.GenomeDefinition;
import org.terasology.genome.GenomeRegistry;
import org.terasology.genome.breed.BreedingAlgorithm;
import org.terasology.genome.breed.SimpleBreedingAlgorithm;
import org.terasology.genome.breed.mutator.GeneMutator;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.genomeMap.SeedBasedGenomeMap;
import org.terasology.genome.system.GenomeManager;
import org.terasology.gf.PlantedSaplingComponent;
import org.terasology.herbalism.HerbEffect;
import org.terasology.herbalism.HerbEffectRegistry;
import org.terasology.herbalism.HerbGeneMutator;
import org.terasology.herbalism.HerbNameProvider;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.HerbHueComponent;
import org.terasology.herbalism.component.PollinatingHerbComponent;
import org.terasology.herbalism.effect.AlterationEffectWrapperHerbEffect;
import org.terasology.herbalism.effect.DoNothingEffect;
import org.terasology.herbalism.effect.HealEffect;
import org.terasology.math.Vector3i;
import org.terasology.randomUpdate.RandomUpdateEvent;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HerbalismAuthoritySystem extends BaseComponentSystem {
    @In
    private GenomeRegistry genomeRegistry;
    @In
    private GenomeManager genomeManager;
    @In
    private WorldProvider worldProvider;
    @In
    private HerbEffectRegistry herbEffectRegistry;
    @In
    private BlockManager blockManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;

    @Override
    public void preBegin() {
        herbEffectRegistry.registerHerbEffect(1f, new DoNothingEffect());
        herbEffectRegistry.registerHerbEffect(1f, new HealEffect());
        herbEffectRegistry.registerHerbEffect(1f, new AlterationEffectWrapperHerbEffect(new WalkSpeedAlterationEffect(), 1f, 1f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationEffectWrapperHerbEffect(new SwimSpeedAlterationEffect(), 1f, 1f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationEffectWrapperHerbEffect(new RegenerationAlterationEffect(), 1f, 100f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationEffectWrapperHerbEffect(new WaterBreathingAlterationEffect(), 1f, 1f));

        final HerbNameProvider herbNameProvider = new HerbNameProvider(worldProvider.getSeed().hashCode());

        int genomeLength = 10;

        GeneMutator herbGeneMutator = new HerbGeneMutator();

        BreedingAlgorithm herbBreedingAlgorithm = new SimpleBreedingAlgorithm(genomeLength, 9, 0.005f, herbGeneMutator);

        SeedBasedGenomeMap herbGenomeMap = new SeedBasedGenomeMap(worldProvider.getSeed().hashCode());
        herbGenomeMap.addSeedBasedProperty(Herbalism.EFFECT_PROPERTY, 1, genomeLength, 3, HerbEffect.class,
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
        herbGenomeMap.addSeedBasedProperty(Herbalism.DURATION_PROPERTY, 1, genomeLength, 2, Long.class,
                new Function<String, Long>() {
                    @Override
                    public Long apply(String input) {
                        int multiplier = input.charAt(0) - 'A' + 1;
                        int duration = 1000 * (input.charAt(1) - 'A' + 1);
                        return (long) (duration * multiplier);
                    }
                });
        herbGenomeMap.addSeedBasedProperty(Herbalism.MAGNITUDE_PROPERTY, 1, genomeLength, 2, Float.class,
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
        herbGenomeMap.addProperty(Herbalism.ICON_PROPERTY, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, TextureRegion.class,
                new Function<String, TextureRegion>() {
                    @Override
                    public TextureRegion apply(String input) {
                        String type = input.substring(0, 1);
                        String genes = input.substring(1, 10);
                        HerbHueComponent herbHue = prefabManager.getPrefab("WoodAndStone:HerbHue" + type).getComponent(HerbHueComponent.class);

                        FastRandom rnd = new FastRandom(genes.hashCode() + 3497987);
                        float[] hueValues = new float[herbHue.hueRanges.size()];
                        for (int i = 0; i < hueValues.length; i++) {
                            String[] hueRangeSplit = herbHue.hueRanges.get(i).split("-");
                            float min = Float.parseFloat(hueRangeSplit[0]);
                            float max = Float.parseFloat(hueRangeSplit[1]);
                            hueValues[i] = rnd.nextFloat(min, max);
                        }

                        return Assets.getTextureRegion(HerbIconAssetResolver.getHerbUri("WoodAndStone:Herb" + type, hueValues));
                    }
                });
        herbGenomeMap.addProperty(Herbalism.PLANTED_BLOCK_PROPERTY, new int[]{0}, Block.class,
                new Function<String, Block>() {
                    @Override
                    public Block apply(String input) {
                        return blockManager.getBlock("WoodAndStone:HerbGrow" + input);
                    }
                });

        GenomeDefinition herbGenomeDefinition = new GenomeDefinition(herbBreedingAlgorithm, herbGenomeMap);

        genomeRegistry.registerType("Herbalism:Herb", herbGenomeDefinition);
    }

    @ReceiveEvent
    public void herbPlanted(SeedPlanted event, EntityRef seedItem, GenomeComponent genomeComponent) {
        Vector3i location = event.getLocation();
        EntityRef plantedEntity = blockEntityRegistry.getEntityAt(location);

        GenomeComponent genome = new GenomeComponent();
        genome.genomeId = genomeComponent.genomeId;
        genome.genes = genomeComponent.genes;

        plantedEntity.addComponent(genome);
    }

    @ReceiveEvent
    public void herbPollination(RandomUpdateEvent event, EntityRef herb, GenomeComponent genome, PollinatingHerbComponent pollinatingHerbComponent, BlockComponent block) {
        Vector3i blockPosition = block.getPosition();

        FastRandom random = new FastRandom();
        // We get 5 tries to pollinate
        for (int i = 0; i < 5; i++) {
            int x = blockPosition.x + random.nextInt(-3, 3);
            int z = blockPosition.z + random.nextInt(-3, 3);
            for (int dY = 1; dY >= -1; dY--) {
                int y = blockPosition.y + dY;
                EntityRef secondHerb = blockEntityRegistry.getExistingEntityAt(new Vector3i(x, y, z));
                if (secondHerb != null && secondHerb.hasComponent(PollinatingHerbComponent.class)
                        && genomeManager.canBreed(herb, secondHerb)) {
                    for (int j = 0; j < 5; j++) {
                        int resultX = blockPosition.x + random.nextInt(-3, 3);
                        int resultZ = blockPosition.z + random.nextInt(-3, 3);
                        for (int resultDY = 1; resultDY >= -1; resultDY--) {
                            int resultY = blockPosition.y + resultDY;
                            Vector3i plantLocation = new Vector3i(resultX, resultY, resultZ);
                            if (worldProvider.getBlock(plantLocation) == BlockManager.getAir()
                                    && blockEntityRegistry.getEntityAt(new Vector3i(resultX, resultY - 1, resultZ)).hasComponent(FarmSoilComponent.class)) {
                                Block plantedBlock = genomeManager.getGenomeProperty(herb, Herbalism.PLANTED_BLOCK_PROPERTY, Block.class);
                                worldProvider.setBlock(plantLocation, plantedBlock);
                                EntityRef plantedHerbEntity = blockEntityRegistry.getEntityAt(plantLocation);
                                plantedHerbEntity.addComponent(new PlantedSaplingComponent());
                                genomeManager.applyBreeding(herb, secondHerb, plantedHerbEntity);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
