// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.generator;

import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;

public final class Blocks {
    private Blocks() {
    }

    public static Block getBlock(String blockId) {
        return CoreRegistry.get(BlockManager.class).getBlock(blockId);
    }
}
