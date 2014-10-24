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
package org.terasology.was.integration.journal;

import com.google.common.base.Supplier;
import org.terasology.asset.Assets;
import org.terasology.crafting.component.CraftingStationComponent;
import org.terasology.crafting.component.CraftingStationIngredientComponent;
import org.terasology.crafting.component.CraftingStationToolComponent;
import org.terasology.crafting.event.CraftingStationUpgraded;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalManager;
import org.terasology.journal.StaticJournalChapterHandler;
import org.terasology.journal.part.DynamicTextJournalPart;
import org.terasology.journal.part.TextJournalPart;
import org.terasology.journal.part.TimestampJournalPart;
import org.terasology.journal.part.TitleJournalPart;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.multiBlock.MultiBlockFormed;
import org.terasology.registry.In;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.seasons.SeasonSystem;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.Arrays;
import java.util.List;

@RegisterSystem
public class SeasonsJournalIntegration extends BaseComponentSystem {
    @In
    private JournalManager journalManager;
    @In
    private SeasonSystem seasonSystem;

    private String chapterId = "Seasons";

    @Override
    public void preBegin() {
        StaticJournalChapterHandler chapterHandler = new StaticJournalChapterHandler();

        List<JournalManager.JournalEntryPart> timeEntry = Arrays.asList(
                new TitleJournalPart("Time of Year"),
                new DynamicTextJournalPart(new Supplier<String>() {
                    @Override
                    public String get() {
                        return "It is " + seasonSystem.getSeasonDayDescription();
                    }
                }, HorizontalAlign.CENTER));

        chapterHandler.registerJournalEntry("1", timeEntry);

        journalManager.registerJournalChapter(chapterId,
                Assets.getTextureRegion("WoodAndStone:journalIcons.WoodAndStone"),
                "Seasons", chapterHandler);
    }

    @ReceiveEvent
    public void playerSpawned(OnPlayerSpawnedEvent event, EntityRef player) {
        player.send(new DiscoveredNewJournalEntry(chapterId, "1"));
    }
}
