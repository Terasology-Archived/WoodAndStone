// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.workstation;

import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface UpgradeRecipe {
    boolean isUpgradeComponent(EntityRef item);

    UpgradeResult getMatchingUpgradeResult(EntityRef station);

    interface UpgradeResult {
        String getResultStationType();

        EntityRef processUpgrade(EntityRef station);
    }
}
