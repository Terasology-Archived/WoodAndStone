package org.terasology.durability;

import org.terasology.entitySystem.Component;
import org.terasology.world.block.ForceBlockActive;
import org.terasology.world.block.items.RetainWhenBlockDetached;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RetainWhenBlockDetached
@ForceBlockActive
public class OverTimeDurabilityReduceComponent implements Component {
}
