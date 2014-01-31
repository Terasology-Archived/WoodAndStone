/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.bronze.system;

import org.terasology.bronze.component.CharcoalPitComponent;
import org.terasology.bronze.event.OpenCharcoalPitRequest;
import org.terasology.bronze.event.ProduceCharcoalRequest;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.AddDelayedActionEvent;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.BlockParticleEffectComponent;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.block.regions.BlockRegionComponent;

import javax.vecmath.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CharcoalPitAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    public static final String PRODUCE_CHARCOAL_ACTION_PREFIX = "Bronze:ProduceCharcoal|";
    @In
    private SlotBasedInventoryManager inventoryManager;
    @In
    private Time time;
    @In
    private PrefabManager prefabManager;
    @In
    private EntityManager entityManager;

    private long lastUpdate;

    @ReceiveEvent(components = {CharcoalPitComponent.class})
    public void userActivatesCharcoalPit(ActivateEvent event, EntityRef entity) {
        entity.send(new OpenCharcoalPitRequest());
    }

    @Override
    public void update(float delta) {
        long gameTimeInMs = time.getGameTimeInMs();
        if (gameTimeInMs + 250 > lastUpdate) {
            for (EntityRef charcoalPit : entityManager.getEntitiesWith(CharcoalPitComponent.class, BlockParticleEffectComponent.class)) {
                BlockParticleEffectComponent particles = charcoalPit.getComponent(BlockParticleEffectComponent.class);
                particles.spawnCount += 5;
                charcoalPit.saveComponent(particles);
            }

            lastUpdate = gameTimeInMs;
        }
    }

    @ReceiveEvent(components = {CharcoalPitComponent.class, BlockRegionComponent.class, InventoryComponent.class})
    public void startBurningCharcoal(ProduceCharcoalRequest event, EntityRef entity) {
        CharcoalPitComponent charcoalPit = entity.getComponent(CharcoalPitComponent.class);

        int logCount = CharcoalPitUtils.getLogCount(inventoryManager, entity);

        if (CharcoalPitUtils.canBurnCharcoal(inventoryManager, logCount, entity)) {
            // Remove logs from inventory
            for (int i = 0; i < charcoalPit.inputSlotCount; i++) {
                EntityRef itemInSlot = inventoryManager.getItemInSlot(entity, i);
                if (itemInSlot.exists()) {
                    inventoryManager.removeItem(entity, itemInSlot);
                }
            }

            int charcoalCount = CharcoalPitUtils.getResultCharcoalCount(logCount, entity);

            int burnLength = 5 * 60 * 1000;

            // Set burn length
            charcoalPit.burnFinishWorldTime = time.getGameTimeInMs() + burnLength;
            entity.saveComponent(charcoalPit);

            Prefab prefab = prefabManager.getPrefab("WoodAndStone:CharcoalPitSmoke");
            BlockParticleEffectComponent particles = prefab.getComponent(BlockParticleEffectComponent.class);
            entity.addComponent(particles);

            BlockRegionComponent region = entity.getComponent(BlockRegionComponent.class);
            Vector3f center = region.region.center();
            Vector3i max = region.region.max();

            LocationComponent location = entity.getComponent(LocationComponent.class);
            location.setWorldPosition(new Vector3f(center.x - 0.5f, max.y + 1, center.z - 0.5f));
            entity.saveComponent(location);

            entity.send(new AddDelayedActionEvent(PRODUCE_CHARCOAL_ACTION_PREFIX + charcoalCount, burnLength));
        }
    }

    @ReceiveEvent(components = {CharcoalPitComponent.class, BlockRegionComponent.class, InventoryComponent.class})
    public void charcoalBurningFinished(DelayedActionTriggeredEvent event, EntityRef entity) {
        String actionId = event.getActionId();
        if (actionId.startsWith(PRODUCE_CHARCOAL_ACTION_PREFIX)) {
            CharcoalPitComponent charcoalPit = entity.getComponent(CharcoalPitComponent.class);

            entity.removeComponent(BlockParticleEffectComponent.class);

            int count = Integer.parseInt(actionId.substring(PRODUCE_CHARCOAL_ACTION_PREFIX.length()));
            for (int i = charcoalPit.inputSlotCount; i < charcoalPit.inputSlotCount + charcoalPit.outputSlotCount; i++) {
                EntityRef itemInSlot = inventoryManager.getItemInSlot(entity, i);
                if (!itemInSlot.exists()) {
                    int toAdd = Math.min(count, 99);
                    EntityRef charcoalItem = entityManager.create("WoodAndStone:Charcoal");
                    ItemComponent item = charcoalItem.getComponent(ItemComponent.class);
                    item.stackCount = (byte) toAdd;
                    charcoalItem.saveComponent(item);
                    inventoryManager.putItemInSlot(entity, i, charcoalItem);
                    count -= toAdd;
                }
                if (count == 0) {
                    break;
                }
            }
        }
    }
}
