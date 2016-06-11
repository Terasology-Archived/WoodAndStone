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
package org.terasology.heat;

import org.terasology.crafting.component.PortableWorkstationComponent;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.heat.component.HeatConsumerComponent;
import org.terasology.heat.component.HeatProducerComponent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.workstation.event.WorkstationStateChanged;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.Iterator;
import java.util.Map;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HeatTriggeringSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final float REMOVE_FUEL_THRESHOLD = 21f;
    private static final float REMOVE_RESIDUAL_HEAT_THRESHOLD = 21f;
    private static final long TRIGGER_INTERVAL = 100;

    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;
    @In
    private Time time;

    private long lastChecked;

    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        if (currentTime > lastChecked + TRIGGER_INTERVAL) {
            PerformanceMonitor.startActivity("Heat - heat update");
            try {
                lastChecked = currentTime;

                for (EntityRef entity : entityManager.getEntitiesWith(HeatConsumerComponent.class, BlockComponent.class)) {
                    HeatConsumerComponent heatConsumer = entity.getComponent(HeatConsumerComponent.class);
                    Iterator<HeatConsumerComponent.ResidualHeat> residualHeatIterator = heatConsumer.residualHeat.iterator();
                    boolean changed = false;
                    while (residualHeatIterator.hasNext()) {
                        HeatConsumerComponent.ResidualHeat residualHeat = residualHeatIterator.next();
                        if (HeatUtils.calculateResidualHeatValue(currentTime, residualHeat) < REMOVE_RESIDUAL_HEAT_THRESHOLD) {
                            residualHeatIterator.remove();
                            changed = true;
                        } else {
                            break;
                        }
                    }
                    if (changed) {
                        entity.saveComponent(heatConsumer);
                    }

                    entity.send(new WorkstationStateChanged());
                }

                for (EntityRef entity : entityManager.getEntitiesWith(HeatProducerComponent.class, BlockComponent.class)) {
                    HeatProducerComponent producer = entity.getComponent(HeatProducerComponent.class);

                    boolean changed = false;
                    Iterator<HeatProducerComponent.FuelSourceConsume> fuelConsumedIterator = producer.fuelConsumed.iterator();
                    while (fuelConsumedIterator.hasNext()) {
                        HeatProducerComponent.FuelSourceConsume fuelSourceConsume = fuelConsumedIterator.next();
                        // If the fuel no longer has any meaningful impact on the producer - remove it
                        if (fuelSourceConsume.startTime + fuelSourceConsume.burnLength < currentTime
                                && HeatUtils.solveHeatEquation(fuelSourceConsume.heatProvided, 20, producer.temperatureLossRate,
                                currentTime - (fuelSourceConsume.startTime + fuelSourceConsume.burnLength)) < REMOVE_FUEL_THRESHOLD) {
                            fuelConsumedIterator.remove();
                            changed = true;
                        } else {
                            break;
                        }
                    }
                    if (changed) {
                        entity.saveComponent(producer);
                    }

                    entity.send(new WorkstationStateChanged());
                }
            } finally {
                PerformanceMonitor.endActivity();
            }



            // TODO: TEST for PortableWorkstations.

            PerformanceMonitor.startActivity("Heat - heat update2");
            try {
                lastChecked = currentTime;

                for (EntityRef entity : entityManager.getEntitiesWith(HeatConsumerComponent.class, PortableWorkstationComponent.class)) {
                    HeatConsumerComponent heatConsumer = entity.getComponent(HeatConsumerComponent.class);
                    Iterator<HeatConsumerComponent.ResidualHeat> residualHeatIterator = heatConsumer.residualHeat.iterator();
                    boolean changed = false;
                    while (residualHeatIterator.hasNext()) {
                        HeatConsumerComponent.ResidualHeat residualHeat = residualHeatIterator.next();
                        if (HeatUtils.calculateResidualHeatValue(currentTime, residualHeat) < REMOVE_RESIDUAL_HEAT_THRESHOLD) {
                            residualHeatIterator.remove();
                            changed = true;
                        } else {
                            break;
                        }
                    }
                    if (changed) {
                        entity.saveComponent(heatConsumer);
                    }

                    entity.send(new WorkstationStateChanged());
                }

                for (EntityRef entity : entityManager.getEntitiesWith(HeatProducerComponent.class, PortableWorkstationComponent.class)) {
                    HeatProducerComponent producer = entity.getComponent(HeatProducerComponent.class);

                    boolean changed = false;
                    Iterator<HeatProducerComponent.FuelSourceConsume> fuelConsumedIterator = producer.fuelConsumed.iterator();
                    while (fuelConsumedIterator.hasNext()) {
                        HeatProducerComponent.FuelSourceConsume fuelSourceConsume = fuelConsumedIterator.next();
                        // If the fuel no longer has any meaningful impact on the producer - remove it
                        if (fuelSourceConsume.startTime + fuelSourceConsume.burnLength < currentTime
                                && HeatUtils.solveHeatEquation(fuelSourceConsume.heatProvided, 20, producer.temperatureLossRate,
                                currentTime - (fuelSourceConsume.startTime + fuelSourceConsume.burnLength)) < REMOVE_FUEL_THRESHOLD) {
                            fuelConsumedIterator.remove();
                            changed = true;
                        } else {
                            break;
                        }
                    }
                    if (changed) {
                        entity.saveComponent(producer);
                    }

                    entity.send(new WorkstationStateChanged());
                }
            } finally {
                PerformanceMonitor.endActivity();
            }
        }
    }

    /**
     * Store residual heat from removed producers into consumers.
     *
     * @param event
     * @param entity
     */
    @ReceiveEvent
    public void beforeProducerRemoved(BeforeRemoveComponent event, EntityRef entity, HeatProducerComponent producer) {
        long gameTime = time.getGameTimeInMs();

        float heat = HeatUtils.calculateHeatForProducer(producer);

        // If this is a portable Workstation entity, this will have neither. So return.
        if (!entity.hasComponent(BlockComponent.class) && !entity.hasComponent(BlockRegionComponent.class))
        {
            return;
        }

        if (entity.hasComponent(PortableWorkstationComponent.class)) {
            return;
        }


        for (Map.Entry<Vector3i, Side> heatedBlock : HeatUtils.getPotentialHeatedBlocksForProducer(entity).entrySet()) {
            EntityRef potentialConsumer = blockEntityRegistry.getEntityAt(heatedBlock.getKey());
            HeatConsumerComponent consumer = potentialConsumer.getComponent(HeatConsumerComponent.class);
            if (consumer != null && consumer.heatDirections.contains(heatedBlock.getValue().reverse())) {
                HeatConsumerComponent.ResidualHeat residualHeat = new HeatConsumerComponent.ResidualHeat();
                residualHeat.time = gameTime;
                residualHeat.baseHeat = heat;
                consumer.residualHeat.add(residualHeat);

                potentialConsumer.saveComponent(consumer);
            }
        }
    }
}
