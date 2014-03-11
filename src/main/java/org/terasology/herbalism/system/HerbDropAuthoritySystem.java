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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.genome.breed.BiodiversityGenerator;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.HerbGeneMutator;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.GeneratedHerbComponent;
import org.terasology.herbalism.component.HerbComponent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.entity.CreateBlockDropsEvent;
import org.terasology.world.block.entity.damage.BlockDamageModifierComponent;

import javax.vecmath.Vector3f;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HerbDropAuthoritySystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private GenomeManager genomeManager;

    private PickupBuilder pickupBuilder;
    private Random random;

    @Override
    public void preBegin() {
        pickupBuilder = new PickupBuilder();
        random = new FastRandom();
    }

    @ReceiveEvent
    public void whenBlockDropped(CreateBlockDropsEvent event, EntityRef blockEntity, GeneratedHerbComponent component) {
        event.consume();
    }

    @ReceiveEvent
    public void whenBlockDropped(CreateBlockDropsEvent event, EntityRef blockEntity, HerbComponent component) {
        event.consume();
    }

    @ReceiveEvent
    public void onGrownHerbDestroyed(DoDestroyEvent event, EntityRef entity, HerbComponent herbComp, GenomeComponent genomeComponent, LocationComponent locationComp) {
        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        float chanceOfBlockDrop = 1;

        if (blockDamageModifierComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageModifierComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            EntityRef herb = entityManager.create("WoodAndStone:HerbBase");

            GenomeComponent genome = new GenomeComponent();
            genome.genomeId = genomeComponent.genomeId;
            genome.genes = genomeComponent.genes;

            herb.addComponent(genome);

            final String herbName = genomeManager.getGenomeProperty(herb, Herbalism.NAME_PROPERTY, String.class);
            DisplayNameComponent displayName = new DisplayNameComponent();
            displayName.name = herbName;
            herb.saveComponent(displayName);

            if (shouldDropToWorld(event, blockDamageModifierComponent, herb)) {
                createDrop(herb, locationComp.getWorldPosition(), false);
            }
        }
    }

    @ReceiveEvent
    public void onGeneratedHerbDestroyed(DoDestroyEvent event, EntityRef entity, GeneratedHerbComponent herbComp, LocationComponent locationComp) {
        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        float chanceOfBlockDrop = 1;

        if (blockDamageModifierComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageModifierComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            final String herbBaseGenome = herbComp.herbBaseGenome;
            final Vector3f position = locationComp.getWorldPosition();

            BiodiversityGenerator generator = new BiodiversityGenerator(worldProvider.getSeed(), 0, new HerbGeneMutator(), herbBaseGenome,
                    3, 0.0002f);
            final String generatedGenes = generator.generateGenes(new Vector2i(TeraMath.floorToInt(position.x + 0.5f), TeraMath.floorToInt(position.z + 0.5f)));

            EntityRef herb = entityManager.create("WoodAndStone:HerbBase");

            GenomeComponent genomeComponent = new GenomeComponent();
            genomeComponent.genomeId = "Herbalism:Herb";
            genomeComponent.genes = generatedGenes;
            herb.addComponent(genomeComponent);

            final String herbName = genomeManager.getGenomeProperty(herb, Herbalism.NAME_PROPERTY, String.class);
            DisplayNameComponent displayName = new DisplayNameComponent();
            displayName.name = herbName;
            herb.saveComponent(displayName);

            if (shouldDropToWorld(event, blockDamageModifierComponent, herb)) {
                createDrop(herb, locationComp.getWorldPosition(), false);
            }
        }
    }

    private boolean shouldDropToWorld(DoDestroyEvent event, BlockDamageModifierComponent blockDamageModifierComponent, EntityRef dropItem) {
        return blockDamageModifierComponent == null || !blockDamageModifierComponent.directPickup
                || !giveItem(event.getInstigator(), dropItem);
    }

    private boolean giveItem(EntityRef instigator, EntityRef dropItem) {
        GiveItemAction giveEvent = new GiveItemAction(instigator, dropItem);
        instigator.send(giveEvent);
        return giveEvent.isConsumed();
    }

    private void createDrop(EntityRef item, Vector3f location, boolean applyMovement) {
        EntityRef pickup = pickupBuilder.createPickupFor(item, location, 60, true);
        if (applyMovement) {
            pickup.send(new ImpulseEvent(random.nextVector3f(30.0f)));
        }
    }

}
