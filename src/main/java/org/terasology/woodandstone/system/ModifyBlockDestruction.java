// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.system;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.health.logic.event.BeforeDamagedEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ModifyBlockDestruction extends BaseComponentSystem {
    private final Set<BlockUri> exceptions = new HashSet<>();

    @Override
    public void initialise() {
        exceptions.add(new BlockUri("CoreAssets:Grass"));
        exceptions.add(new BlockUri("CoreAssets:Dirt"));
        exceptions.add(new BlockUri("CoreAssets:Sand"));
        exceptions.add(new BlockUri("WoodAndStone:ClayStone"));

        // TODO: Probably a better way to "whitelist" these leaf blocks in bulk
        exceptions.add(new BlockUri("PlantPack:BirchLeaf"));
        exceptions.add(new BlockUri("PlantPack:BlueSpruceLeaf"));
        exceptions.add(new BlockUri("PlantPack:BroomLeaf"));
        exceptions.add(new BlockUri("PlantPack:CypressLeaf"));
        exceptions.add(new BlockUri("PlantPack:GrandMapleLeaf"));
        exceptions.add(new BlockUri("PlantPack:LongPineLeaf"));
        exceptions.add(new BlockUri("PlantPack:MapleLeaf"));
        exceptions.add(new BlockUri("PlantPack:MirthrootLeaf"));
        exceptions.add(new BlockUri("PlantPack:OakLeaf"));
        exceptions.add(new BlockUri("PlantPack:PineLeaf"));
        exceptions.add(new BlockUri("PlantPack:SakuraLeaf"));
        exceptions.add(new BlockUri("PlantPack:SpruceLeaf"));
        exceptions.add(new BlockUri("PlantPack:StisusLeaf"));
    }

    @ReceiveEvent
    public void preventPlayerFromDestroyingBasicBlocksByHand(BeforeDamagedEvent event, EntityRef blockEntity) {
        BlockComponent blockComponent = blockEntity.getComponent(BlockComponent.class);
        if (blockComponent != null && event.getInstigator().hasComponent(CharacterComponent.class)) {
            Block block = blockComponent.getBlock();

            if (exceptions.contains(block.getURI())) {
                return;
            }

            Iterable<String> categoriesIterator = block.getBlockFamily().getCategories();
            if (!canBeDestroyedByBlockDamage(categoriesIterator, event.getDamageType())) {
                event.consume();
            }
        }
    }

    private boolean canBeDestroyedByBlockDamage(Iterable<String> categoriesIterator, Prefab damageType) {
        if (categoriesIterator.iterator().hasNext()) {
            // If this block has a category, then it HAS to be destroyed by a tool with that category
            BlockDamageModifierComponent blockDamage = damageType.getComponent(BlockDamageModifierComponent.class);
            if (blockDamage == null) {
                return false;
            }
            for (String category : categoriesIterator) {
                if (blockDamage.materialDamageMultiplier.containsKey(category)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
