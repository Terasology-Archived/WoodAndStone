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
package org.terasology.npcAI.system;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.module.health.events.OnDamagedEvent;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.registry.In;
import org.terasology.math.TeraMath;
import org.terasology.npcAI.component.AggroComponent;
import org.terasology.npcAI.component.ThreatMultiplierComponent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class AggroSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final float MAX_AGGRO_PERIOD = 10000;
    private static final long TRIGGER_INTERVAL = 100;
    private static final int AGGRO_VALUES_MAX_MEMORY = 20;

    @In
    private Time time;
    @In
    private EntityManager entityManager;

    private long lastChecked;

    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        if (currentTime > lastChecked + TRIGGER_INTERVAL) {
            PerformanceMonitor.startActivity("Aggro - aggro update");
            try {
                lastChecked = currentTime;

                for (EntityRef entity : entityManager.getEntitiesWith(AggroComponent.class)) {
                    AggroComponent aggroComponent = entity.getComponent(AggroComponent.class);

                    // Remove old values and get all aggro components per entity
                    Map<EntityRef, Float> aggroValues = new HashMap<>();
                    final Iterator<AggroComponent.AggroValue> iterator = aggroComponent.aggroValues.iterator();
                    while (iterator.hasNext()) {
                        final AggroComponent.AggroValue aggroValue = iterator.next();
                        final float aggroPart = getAggroValue(currentTime, aggroValue);
                        if (aggroPart == 0 || aggroComponent.aggroValues.size() > AGGRO_VALUES_MAX_MEMORY) {
                            iterator.remove();
                        } else {
                            final Float currentValue = aggroValues.get(aggroValue.instigator);
                            if (currentValue == null) {
                                aggroValues.put(aggroValue.instigator, aggroPart);
                            } else {
                                aggroValues.put(aggroValue.instigator, currentValue + aggroPart);
                            }
                        }
                    }

                    float maxValue = 0f;
                    EntityRef highestAggroEntity = EntityRef.NULL;
                    for (Map.Entry<EntityRef, Float> entityAggro : aggroValues.entrySet()) {
                        if (entityAggro.getValue() > maxValue) {
                            maxValue = entityAggro.getValue();
                            highestAggroEntity = entityAggro.getKey();
                        }
                    }

                    if (aggroComponent.aggroTarget != highestAggroEntity) {
                        aggroComponent.aggroTarget = highestAggroEntity;
                        entity.saveComponent(aggroComponent);
                    }
                }
            } finally {
                PerformanceMonitor.endActivity();
            }
        }
    }

    @ReceiveEvent
    public void registerAggro(OnDamagedEvent event, EntityRef damagedEntity, AggroComponent aggro) {
        final ThreatMultiplierComponent threatMultiplier = event.getType().getComponent(ThreatMultiplierComponent.class);
        float threatMultiplierValue = 1f;
        if (threatMultiplier != null) {
            threatMultiplierValue = threatMultiplier.threatMultiplier;
        }

        AggroComponent.AggroValue aggroValue = new AggroComponent.AggroValue();
        aggroValue.instigator = event.getInstigator();
        aggroValue.amount = TeraMath.floorToInt(event.getDamageAmount() * threatMultiplierValue);
        aggroValue.time = time.getGameTimeInMs();
        aggro.aggroValues.add(aggroValue);
        damagedEntity.saveComponent(aggro);
    }

    private float getAggroValue(long currentTime, AggroComponent.AggroValue aggroValue) {
        float multiplier = 1 - ((currentTime - aggroValue.time) / MAX_AGGRO_PERIOD);
        if (multiplier < 0) {
            return 0f;
        }
        return aggroValue.amount * multiplier;
    }
}
