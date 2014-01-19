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
@RegisterWorldGenerator(id = "example", displayName = "Example generator", description = "Generates ore")
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
                        new PDist(0.1f, 0f), new PDist(60f, 15f, PDist.Type.normal), new PDist(10f, 2f), new PDist(5f, 1f),
                        new PDist(0f, 0.5f), new PDist(1f, 0f), new PDist(3f, 0f), new PDist(1f, 0f),
                        new PDist(0.2f, 0f)));

        blockGenerator.addStructureGenerator(
                new ClusterStructureGenerator(blockManager.getBlock("core:CoalOre"),
                        new PDist(0.8f, 0f), new PDist(80f, 15f, PDist.Type.uniform), new PDist(8f, 2f)));

        register(blockGenerator);
    }
}
