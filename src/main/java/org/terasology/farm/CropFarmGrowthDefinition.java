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
package org.terasology.farm;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.terasology.anotherWorld.LocalParameters;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.farm.component.FarmSoilComponent;
import org.terasology.gf.grass.CropGrowthDefinition;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockUri;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CropFarmGrowthDefinition extends CropGrowthDefinition {
    public CropFarmGrowthDefinition(String plantId, List<BlockUri> plantStages, long growthInterval,
                                    Predicate<LocalParameters> spawnCondition, Function<LocalParameters, Float> growthChance) {
        super(plantId, plantStages, growthInterval, spawnCondition, growthChance);
    }

    @Override
    protected float getGrowthChance(WorldProvider worldProvider, Vector3i position) {
        float chance = super.getGrowthChance(worldProvider, position);
        Vector3i soilPosition = new Vector3i(position.x, position.y - 1, position.z);
        EntityRef soilEntity = CoreRegistry.get(BlockEntityRegistry.class).getEntityAt(soilPosition);
        FarmSoilComponent soil = soilEntity.getComponent(FarmSoilComponent.class);
        if (soil != null) {
            chance = chance * soil.growChanceMultiplier;
        }
        return (float) TeraMath.clamp(chance);
    }
}
