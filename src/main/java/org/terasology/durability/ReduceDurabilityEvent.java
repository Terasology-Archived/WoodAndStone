// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.durability;

import org.terasology.engine.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ReduceDurabilityEvent implements Event {
    private final int reduceBy;

    public ReduceDurabilityEvent(int reduceBy) {
        this.reduceBy = reduceBy;
    }

    public int getReduceBy() {
        return reduceBy;
    }
}
