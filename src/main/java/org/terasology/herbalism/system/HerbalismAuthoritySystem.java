// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.system;

import org.joml.Vector3i;
import org.terasology.anotherWorldPlants.farm.component.FarmSoilComponent;
import org.terasology.anotherWorldPlants.farm.event.SeedPlanted;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.gf.PlantedSaplingComponent;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.PollinatingHerbComponent;
import org.terasology.randomUpdate.RandomUpdateEvent;

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
        Vector3i blockPosition = block.getPosition(new Vector3i());

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
