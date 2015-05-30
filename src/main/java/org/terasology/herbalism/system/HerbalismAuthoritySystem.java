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

import org.terasology.anotherWorldPlants.farm.component.FarmSoilComponent;
import org.terasology.anotherWorldPlants.farm.event.SeedPlanted;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.gf.PlantedSaplingComponent;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.PollinatingHerbComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.randomUpdate.RandomUpdateEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HerbalismAuthoritySystem extends BaseComponentSystem {
    @In
    private GenomeManager genomeManager;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

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
                            if (worldProvider.getBlock(plantLocation).isPenetrable()
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
