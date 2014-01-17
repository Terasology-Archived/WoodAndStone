package org.terasology.was.system;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.was.component.BlockDropGrammarComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BeforeBlockToItem;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockDropGrammarSystem implements ComponentSystem {
    private WorldProvider worldProvider;
    private EntityManager entityManager;
    private BlockManager blockManager;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        entityManager = CoreRegistry.get(EntityManager.class);
        blockManager = CoreRegistry.get(BlockManager.class);
    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent(components = {BlockDropGrammarComponent.class})
    public void whenBlockDropped(BeforeBlockToItem event, EntityRef blockEntity) {
        BlockDropGrammarComponent blockDrop = blockEntity.getComponent(BlockDropGrammarComponent.class);

        // Remove the "default" block drop
        event.removeDefaultBlock();

        FastRandom rnd = new FastRandom();

        if (blockDrop.blockDrops != null) {
            for (String drop : blockDrop.blockDrops) {
                boolean dropping = true;
                int pipeIndex = drop.indexOf('|');
                if (pipeIndex > -1) {
                    float chance = Float.parseFloat(drop.substring(0, pipeIndex));
                    if (rnd.nextFloat() >= chance)
                        dropping = false;
                    drop = drop.substring(pipeIndex + 1);
                }
                if (dropping) {
                    DropParser dropParser = new DropParser(rnd, drop).invoke();
                    event.addBlockToGenerate(blockManager.getBlockFamily(dropParser.getDrop()), dropParser.getCount());
                }
            }
        }

        if (blockDrop.itemDrops != null) {
            for (String drop : blockDrop.itemDrops) {
                boolean dropping = true;
                int pipeIndex = drop.indexOf('|');
                if (pipeIndex > -1) {
                    float chance = Float.parseFloat(drop.substring(0, pipeIndex));
                    if (rnd.nextFloat() >= chance)
                        dropping = false;
                    drop = drop.substring(pipeIndex + 1);
                }
                if (dropping) {
                    DropParser dropParser = new DropParser(rnd, drop).invoke();
                    EntityRef entityRef = entityManager.create(dropParser.getDrop());
                    if (dropParser.getCount() > 1) {
                        ItemComponent itemComponent = entityRef.getComponent(ItemComponent.class);
                        itemComponent.stackCount = (byte) dropParser.getCount();
                    }
                    event.addItemToGenerate(entityRef);
                }
            }
        }
    }

    private class DropParser {
        private FastRandom rnd;
        private String drop;
        private int count;

        public DropParser(FastRandom rnd, String drop) {
            this.rnd = rnd;
            this.drop = drop;
        }

        public String getDrop() {
            return drop;
        }

        public int getCount() {
            return count;
        }

        public DropParser invoke() {
            int timesIndex = drop.indexOf('*');
            int countMin = 1;
            int countMax = 1;
            if (timesIndex > -1) {
                String timesStr = drop.substring(0, timesIndex);
                int minusIndex = timesStr.indexOf('-');
                if (minusIndex > -1) {
                    countMin = Integer.parseInt(timesStr.substring(0, minusIndex));
                    countMax = Integer.parseInt(timesStr.substring(minusIndex + 1));
                } else
                    countMin = countMax = Integer.parseInt(timesStr);
                drop = drop.substring(timesIndex + 1);
            }
            count = rnd.nextInt(countMin, countMax);
            return this;
        }
    }
}
