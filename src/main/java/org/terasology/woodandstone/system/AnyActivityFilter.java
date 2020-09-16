// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.system;

import com.google.common.base.Predicate;
import org.terasology.engine.logic.common.ActivateEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class AnyActivityFilter implements Predicate<ActivateEvent> {
    @Override
    public boolean apply(ActivateEvent input) {
        return true;
    }
}
