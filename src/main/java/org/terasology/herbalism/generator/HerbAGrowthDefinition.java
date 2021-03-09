// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.generator;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.gf.grass.ReplaceBlockGrowthDefinition;

import java.util.Arrays;

@RegisterPlugin
public class HerbAGrowthDefinition extends ReplaceBlockGrowthDefinition {
    public static final String ID = "Herbalism:Herb";

    public HerbAGrowthDefinition() {
        super(ID, Arrays.asList(
                        new BlockUri("WoodAndStone:HerbGrowA"), new BlockUri("WoodAndStone:HerbGrownA"), new BlockUri("CoreAssets:DeadBush")),
                50 * 1000, 200 * 1000,
                input -> input.getHumidity() > 0.2f && input.getTemperature() > 15f,
                input -> 0.2f * input.getHumidity()
        );
    }

    @Override
    protected void replaceBlock(WorldProvider worldProvider, BlockManager blockManager, EntityRef plant, Vector3i position, BlockUri nextStage, boolean isLast) {
        if (!isLast) {
            // We need to copy the genome between stages
            final GenomeComponent genome = plant.getComponent(GenomeComponent.class);

            GenomeComponent genomeCopy = new GenomeComponent();
            genomeCopy.genomeId = genome.genomeId;
            genomeCopy.genes = genome.genes;

            super.replaceBlock(worldProvider, blockManager, plant, position, nextStage, isLast);

            final EntityRef blockEntity = CoreRegistry.get(BlockEntityRegistry.class).getEntityAt(position);
            blockEntity.addComponent(genomeCopy);
        } else {
            super.replaceBlock(worldProvider, blockManager, plant, position, nextStage, isLast);
        }
    }
}
