// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.system;

import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.health.DoDestroyEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.events.ImpulseEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.entity.CreateBlockDropsEvent;
import org.terasology.engine.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.genome.breed.BiodiversityGenerator;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.HerbGeneMutator;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.GeneratedHerbComponent;
import org.terasology.herbalism.component.HerbComponent;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HerbDropAuthoritySystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private GenomeManager genomeManager;
    @In
    private InventoryManager inventoryManager;

    private Random random;

    @Override
    public void preBegin() {
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
    public void onGrownHerbDestroyed(DoDestroyEvent event, EntityRef entity, HerbComponent herbComp,
                                     GenomeComponent genomeComponent, LocationComponent locationComp) {
        BlockDamageModifierComponent blockDamageModifierComponent =
            event.getDamageType().getComponent(BlockDamageModifierComponent.class);
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

            final ItemComponent item = herb.getComponent(ItemComponent.class);
            item.icon = genomeManager.getGenomeProperty(herb, Herbalism.ICON_PROPERTY, TextureRegionAsset.class);
            herb.saveComponent(item);

            if (shouldDropToWorld(event, blockDamageModifierComponent, herb)) {
                createDrop(herb, locationComp.getWorldPosition(new Vector3f()), false);
            }
        }
    }

    @ReceiveEvent
    public void onGeneratedHerbDestroyed(DoDestroyEvent event, EntityRef entity, GeneratedHerbComponent herbComp,
                                         LocationComponent locationComp) {
        BlockDamageModifierComponent blockDamageModifierComponent =
            event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        float chanceOfBlockDrop = 1;

        if (blockDamageModifierComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageModifierComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            final String herbBaseGenome = herbComp.herbBaseGenome;
            final Vector3i position = new Vector3i(locationComp.getWorldPosition(new Vector3f()), RoundingMode.HALF_UP);

            BiodiversityGenerator generator = new BiodiversityGenerator(worldProvider.getSeed(), 0,
                new HerbGeneMutator(), herbBaseGenome,
                3, 0.0002f);
            final String generatedGenes = generator.generateGenes(new Vector2i(position.x, position.y));

            EntityRef herb = entityManager.create("WoodAndStone:HerbBase");

            GenomeComponent genomeComponent = new GenomeComponent();
            genomeComponent.genomeId = "Herbalism:Herb";
            genomeComponent.genes = generatedGenes;
            herb.addComponent(genomeComponent);

            final ItemComponent item = herb.getComponent(ItemComponent.class);
            item.icon = genomeManager.getGenomeProperty(herb, Herbalism.ICON_PROPERTY, TextureRegionAsset.class);
            herb.saveComponent(item);

            if (shouldDropToWorld(event, blockDamageModifierComponent, herb)) {
                createDrop(herb, locationComp.getWorldPosition(new Vector3f()), false);
            }
        }
    }

    private boolean shouldDropToWorld(DoDestroyEvent event, BlockDamageModifierComponent blockDamageModifierComponent
        , EntityRef dropItem) {
        EntityRef instigator = event.getInstigator();
        return blockDamageModifierComponent == null || !blockDamageModifierComponent.directPickup
            || !inventoryManager.giveItem(instigator, instigator, dropItem);
    }

    private void createDrop(EntityRef item, Vector3f location, boolean applyMovement) {
        item.send(new DropItemEvent(location));
        if (applyMovement) {
            item.send(new ImpulseEvent(random.nextVector3f(30.0f, new Vector3f())));
        }
    }
}
