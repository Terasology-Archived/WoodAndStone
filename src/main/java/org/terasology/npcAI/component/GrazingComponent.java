// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.npcAI.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.Block;

import java.util.List;

public class GrazingComponent implements Component {
    public List<Block> grazesOnBlocks;
    public int visionDistance;
    public long grazingTime;
}
