package org.terasology.oregeneration;

import com.google.common.collect.Sets;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.core.world.generator.chunkGenerators.FlatTerrainGenerator;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.oregeneration.util.PDist;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generator.RegisterWorldGenerator;

import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterWorldGenerator(id = "debug", displayName = "Debug generator", description = "Generates ore, and removes any other ore")
public class ExampleGenerator extends AbstractBaseWorldGenerator {

    public ExampleGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    public void initialize() {
        register(new FlatTerrainGenerator(100));
        Set<Block> blocksToReplace = Sets.newHashSet();
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        blocksToReplace.add(blockManager.getBlock("core:Stone"));
        blocksToReplace.add(blockManager.getBlock("core:Dirt"));

        StructureBasedBlockGenerator blockGenerator = new StructureBasedBlockGenerator(blocksToReplace);
        blockGenerator.setDebug(true);

        blockGenerator.addStructureGenerator(
                new PocketStructureGenerator(blockManager.getBlock("core:CopperOre"),
                        new PDist(0.1f, 0f), new PDist(12f, 5f), new PDist(4f, 2f), new PDist(60f, 16f, PDist.Type.normal),
                        new PDist(0f, 0.35f), new PDist(1f, 0.1f), new PDist(0.1f, 0f), new PDist(0.2f, 0f), new PDist(0.5f, 0)));

        blockGenerator.addStructureGenerator(
                new ClusterStructureGenerator(blockManager.getBlock("core:CoalOre"),
                        new PDist(0.8f, 0f), new PDist(80f, 15f, PDist.Type.uniform), new PDist(8f, 2f)));

        blockGenerator.addStructureGenerator(
                new VeinStructureGenerator(blockManager.getBlock("core:GoldOre"), blockManager.getBlock("core:GoldOre"),
                        new PDist(0.25f, 0f), new PDist(3f, 1f), new PDist(20F, 10F, PDist.Type.normal),
                        new PDist(3F, 2F), new PDist(0F, 0.55F), new PDist(40F, 0F), new PDist(16F, 0F),
                        new PDist(5F, 0F), new PDist(0.5F, 0.5F), new PDist(0.5F, 0.2F),
                        new PDist(1F, 0F), new PDist(1F, 0.1F)));

        register(blockGenerator);
    }
}
