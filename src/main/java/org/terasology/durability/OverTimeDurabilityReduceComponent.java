// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.durability;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.engine.world.block.items.AddToBlockBasedItem;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@AddToBlockBasedItem
@ForceBlockActive
public class OverTimeDurabilityReduceComponent implements Component {
}
