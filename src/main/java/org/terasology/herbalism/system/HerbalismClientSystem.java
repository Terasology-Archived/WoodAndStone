/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.herbalism.system;

import org.terasology.anotherWorldPlants.farm.component.SeedComponent;
import org.terasology.utilities.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.HerbComponent;
import org.terasology.herbalism.component.PotionComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.widgets.TooltipLine;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class HerbalismClientSystem extends BaseComponentSystem {
    @In
    private GenomeManager genomeManager;

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

    public static TooltipLine getHerbTooltipLine(String herbName) {
        return new TooltipLine("Specie: " + herbName, Assets.getSkin("WoodAndStone:herbTooltip").get());
    }
}
