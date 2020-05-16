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
package org.terasology.was.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.health.event.BeforeDamagedEvent;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.entity.damage.BlockDamageModifierComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ModifyBlockDestruction extends BaseComponentSystem {
    private Set<BlockUri> exceptions = new HashSet<>();

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
