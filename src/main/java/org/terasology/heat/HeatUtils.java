// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.heat;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.heat.component.HeatConsumerComponent;
import org.terasology.heat.component.HeatProducerComponent;
import org.terasology.math.Side;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class HeatUtils {
    public static final float HEAT_MAGIC_VALUE = 2000f;

    private HeatUtils() {

    }

    public static float calculateHeatForProducer(HeatProducerComponent producer) {
        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

        return Math.min(producer.maximumTemperature, calculateHeatForProducerAtTime(producer, gameTime));
    }

    public static float solveHeatEquation(float startingHeat, float appliedHeat, float heatTransferEfficiency, long duration) {
        return startingHeat + (appliedHeat - startingHeat) * (1 - (float) Math.pow(Math.E, -duration * heatTransferEfficiency / HEAT_MAGIC_VALUE));
    }

    private static float calculateHeatForProducerAtTime(HeatProducerComponent producer, long time) {
        float heat = 20;
        long lastCalculated = 0;
        for (HeatProducerComponent.FuelSourceConsume fuelSourceConsume : producer.fuelConsumed) {
            if (fuelSourceConsume.startTime < time) {
                if (lastCalculated < fuelSourceConsume.startTime) {
                    heat = solveHeatEquation(heat, 20, producer.temperatureLossRate, fuelSourceConsume.startTime - lastCalculated);
                    lastCalculated = fuelSourceConsume.startTime;
                }
                long heatEndTime = Math.min(fuelSourceConsume.startTime + fuelSourceConsume.burnLength, time);
                heat = Math.min(producer.maximumTemperature,
                        solveHeatEquation(heat, fuelSourceConsume.heatProvided, producer.temperatureAbsorptionRate, heatEndTime - lastCalculated));
                lastCalculated = heatEndTime;
            } else {
                break;
            }
        }

        if (lastCalculated < time) {
            heat = solveHeatEquation(heat, 20, producer.temperatureLossRate, time - lastCalculated);
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
            return 20;
        }
    }

    private static float calculateHeatForConsumer(EntityRef entity, BlockEntityRegistry blockEntityRegistry, HeatConsumerComponent heatConsumer) {
        float result = 20;

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

    public static BlockRegion getEntityBlocks(EntityRef entityRef) {
        BlockComponent blockComponent = entityRef.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            Vector3i blockPosition = blockComponent.getPosition(new Vector3i());
            return new BlockRegion(blockPosition).union(blockPosition);
        }
        BlockRegionComponent blockRegionComponent = entityRef.getComponent(BlockRegionComponent.class);
        return blockRegionComponent.region;
    }

    public static Map<Vector3i, Side> getPotentialHeatSourceBlocksForConsumer(EntityRef consumer) {
        HeatConsumerComponent consumerComp = consumer.getComponent(HeatConsumerComponent.class);
        if (consumerComp == null) {
            return Collections.emptyMap();
        }

        BlockRegion entityBlocks = getEntityBlocks(consumer);

        Map<Vector3i, Side> result = new HashMap<>();

        for (Vector3ic entityBlock : entityBlocks) {
            for (Side heatDirection : consumerComp.heatDirections) {
                Vector3i heatedBlock = new Vector3i(entityBlock);
                heatedBlock.add(heatDirection.direction());
                if (!entityBlocks.contains(heatedBlock)) {
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

        BlockRegion entityBlocks = getEntityBlocks(producer);

        Map<Vector3i, Side> result = new HashMap<>();

        for (Vector3ic entityBlock : entityBlocks) {
            for (Side heatDirection : producerComp.heatDirections) {
                Vector3i heatedBlock = new Vector3i(entityBlock);
                heatedBlock.add(heatDirection.direction());
                if (!entityBlocks.contains(heatedBlock)) {
                    result.put(heatedBlock, heatDirection);
                }
            }
        }

        return result;
    }
}
