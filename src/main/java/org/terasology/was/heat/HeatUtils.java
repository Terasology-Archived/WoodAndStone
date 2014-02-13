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
package org.terasology.was.heat;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class HeatUtils {
    private HeatUtils() {

    }

    public static float doCalculationForOneFuelSourceConsume(float heatStorageEfficiency, long gameTime,
                                                             HeatProducerComponent.FuelSourceConsume fuelSourceConsume) {
        float secondsSinceEnd = (gameTime - fuelSourceConsume.startTime - fuelSourceConsume.burnLength) / 1000f;

        if (secondsSinceEnd > 0) {
            // Finished burning - utilise formula for continuous compounding to calculate cumulative loss of heat - (e^(-(1/efficiency)*time))
            return fuelSourceConsume.heatProvided * (float) Math.pow(Math.E, -(1 / heatStorageEfficiency) * secondsSinceEnd);
        } else {
            return fuelSourceConsume.heatProvided * (gameTime - fuelSourceConsume.startTime) / fuelSourceConsume.burnLength;
        }
    }

    public static float calculateHeatForProducer(HeatProducerComponent producer) {
        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

        float heat = 0;
        for (HeatProducerComponent.FuelSourceConsume fuelSourceConsume : producer.fuelConsumed) {
            heat += doCalculationForOneFuelSourceConsume(producer.heatStorageEfficiency, gameTime, fuelSourceConsume);
        }

        return heat;
    }

    public static float calculateHeatForEntity(EntityRef entity, BlockEntityRegistry blockEntityRegistry) {
        HeatProducerComponent producer = entity.getComponent(HeatProducerComponent.class);
        HeatConsumerComponent consumer = entity.getComponent(HeatConsumerComponent.class);
        if (producer != null) {
            return calculateHeatForProducer(producer);
        } else if (consumer != null) {
            return calculateHeatForConsumer(entity, blockEntityRegistry, consumer);
        } else {
            return 0;
        }
    }

    private static float calculateHeatForConsumer(EntityRef entity, BlockEntityRegistry blockEntityRegistry, HeatConsumerComponent heatConsumer) {
        float result = 0;

        for (Map.Entry<Vector3i, Side> heaterBlock : getPotentialHeatSourceBlocksForConsumer(entity).entrySet()) {
            EntityRef potentialHeatProducer = blockEntityRegistry.getEntityAt(heaterBlock.getKey());
            HeatProducerComponent producer = potentialHeatProducer.getComponent(HeatProducerComponent.class);

            if (producer != null && producer.heatDirections.contains(heaterBlock.getValue().reverse())) {
                result += calculateHeatForProducer(producer);
            }
        }

        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

        for (HeatConsumerComponent.ResidualHeat residualHeat : heatConsumer.residualHeat) {
            double heat = calculateResidualHeatValue(gameTime, residualHeat);
            result += heat;
        }

        return result * heatConsumer.heatConsumptionEfficiency;
    }

    public static double calculateResidualHeatValue(long gameTime, HeatConsumerComponent.ResidualHeat residualHeat) {
        float timeSinceHeatWasEstablished = (gameTime - residualHeat.time) / 1000f;
        return residualHeat.baseHeat * Math.pow(Math.E, -1 * timeSinceHeatWasEstablished);
    }

    public static Region3i getEntityBlocks(EntityRef entityRef) {
        BlockComponent blockComponent = entityRef.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            Vector3i blockPosition = blockComponent.getPosition();
            return Region3i.createBounded(blockPosition, blockPosition);
        }
        BlockRegionComponent blockRegionComponent = entityRef.getComponent(BlockRegionComponent.class);
        return blockRegionComponent.region;
    }

    public static Map<Vector3i, Side> getPotentialHeatSourceBlocksForConsumer(EntityRef consumer) {
        HeatConsumerComponent consumerComp = consumer.getComponent(HeatConsumerComponent.class);
        if (consumerComp == null) {
            return Collections.emptyMap();
        }

        Region3i entityBlocks = getEntityBlocks(consumer);

        Map<Vector3i, Side> result = new HashMap<>();

        for (Vector3i entityBlock : entityBlocks) {
            for (Side heatDirection : consumerComp.heatDirections) {
                Vector3i heatedBlock = entityBlock.clone();
                heatedBlock.add(heatDirection.getVector3i());
                if (!entityBlocks.encompasses(heatedBlock)) {
                    result.put(heatedBlock, heatDirection);
                }
            }
        }

        return result;
    }

    public static Map<Vector3i, Side> getPotentialHeatedBlocksForProducer(EntityRef producer) {
        HeatProducerComponent producerComp = producer.getComponent(HeatProducerComponent.class);
        if (producerComp == null) {
            return Collections.emptyMap();
        }

        Region3i entityBlocks = getEntityBlocks(producer);

        Map<Vector3i, Side> result = new HashMap<>();

        for (Vector3i entityBlock : entityBlocks) {
            for (Side heatDirection : producerComp.heatDirections) {
                Vector3i heatedBlock = entityBlock.clone();
                heatedBlock.add(heatDirection.getVector3i());
                if (!entityBlocks.encompasses(heatedBlock)) {
                    result.put(heatedBlock, heatDirection);
                }
            }
        }

        return result;
    }
}
