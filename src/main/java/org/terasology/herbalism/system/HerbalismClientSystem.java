// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.system;

import org.terasology.anotherWorldPlants.farm.component.SeedComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.HerbComponent;
import org.terasology.herbalism.component.PotionComponent;
import org.terasology.inventory.rendering.nui.layers.ingame.GetItemTooltip;
import org.terasology.nui.widgets.TooltipLine;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class HerbalismClientSystem extends BaseComponentSystem {
    @In
    private GenomeManager genomeManager;

    public static TooltipLine getHerbTooltipLine(String herbName) {
        return new TooltipLine("Specie: " + herbName, Assets.getSkin("WoodAndStone:herbTooltip").get());
    }

    @ReceiveEvent
    public void getHerbTooltip(GetItemTooltip tooltip, EntityRef item, HerbComponent herb, GenomeComponent genome) {
        appendSpecie(tooltip, item);
    }

    @ReceiveEvent
    public void getHerbTooltip(GetItemTooltip tooltip, EntityRef item, SeedComponent herb, GenomeComponent genome) {
        appendSpecie(tooltip, item);
    }

    @ReceiveEvent
    public void getHerbTooltip(GetItemTooltip tooltip, EntityRef item, PotionComponent herb, GenomeComponent genome) {
        appendSpecie(tooltip, item);
    }

    private void appendSpecie(GetItemTooltip tooltip, EntityRef item) {
        String herbName = genomeManager.getGenomeProperty(item, Herbalism.NAME_PROPERTY, String.class);
        tooltip.getTooltipLines().add(getHerbTooltipLine(herbName));
    }
}
