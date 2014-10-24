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
package org.terasology.herbalism.generator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.terasology.anotherWorld.LocalParameters;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.gf.grass.ReplaceBlockGrowthDefinition;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Arrays;

@RegisterPlugin
public class HerbAGrowthDefinition extends ReplaceBlockGrowthDefinition {
    public static final String ID = "Herbalism:Herb";

    public HerbAGrowthDefinition() {
        super(ID, Arrays.asList(
                new BlockUri("WoodAndStone", "HerbGrowA"), new BlockUri("WoodAndStone", "HerbGrownA"), new BlockUri("Core", "DeadBush")),
                50 * 1000, 200 * 1000,
                new Predicate<LocalParameters>() {
                    @Override
                    public boolean apply(LocalParameters input) {
                        return input.getHumidity() > 0.2f && input.getTemperature() > 15f;
                    }
                },
                new Function<LocalParameters, Float>() {
                    @Override
                    public Float apply(LocalParameters input) {
                        return 0.2f * input.getHumidity();
                    }
                }
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
