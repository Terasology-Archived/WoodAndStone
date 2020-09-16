// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.system;

import com.google.common.base.Predicate;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.math.Side;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UseOnTopFilter implements Predicate<ActivateEvent> {
    @Override
    public boolean apply(ActivateEvent event) {
        Side side = Side.inDirection(event.getHitNormal());
        return side == Side.TOP;
    }
}
