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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalManager;
import org.terasology.journal.StaticJournalChapterHandler;
import org.terasology.journal.part.TextJournalPart;
import org.terasology.journal.part.TitleJournalPart;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class FarmingAndCookingJournalIntegration extends BaseComponentSystem {
    @In
    private JournalManager journalManager;
    @In
    private PrefabManager prefabManager;
    @In
    private BlockManager blockManager;

    private String chapterId = "FarmingAndCooking";
    private Multimap<String, String> dependencyMap = HashMultimap.create();

    @Override
    public void preBegin() {
        StaticJournalChapterHandler chapterHandler = new StaticJournalChapterHandler();

        Prefab stoneItem = prefabManager.getPrefab("WoodAndStone:stone");
        Prefab stickItem = prefabManager.getPrefab("WoodAndStone:stick");

        Block quernBlock = blockManager.getBlockFamily("WoodAndStone:quern").getArchetypeBlock();

        List<JournalManager.JournalEntryPart> introduction = Arrays.asList(
                new TitleJournalPart("Introduction"),
                new TextJournalPart("This part of your journal will guide you through farming and cooking activities required " +
                        "to survive in Throughout the Ages world."));
        chapterHandler.registerJournalEntry("introduction", introduction);

        List<JournalManager.JournalEntryPart> quern = Arrays.asList(
                new TitleJournalPart("Quern"),
                new TextJournalPart("Quern, also known as a hand mill, allows you to grind various substances, including grain. " +
                        "It is also useful for grinding crystals into dust for easier metal extraction.\n\nTo craft a" +
                        " quern you need six Stones and two Sticks."),
                new RecipeJournalPart(new Block[2], new Prefab[]{stoneItem, stickItem}, quernBlock, null, 1),
                new TextJournalPart("Using a hammer you can then create a Quern at the stone working station."));
        chapterHandler.registerJournalEntry("quern", quern);

        dependencyMap.put("quern", "introduction");

        List<JournalManager.JournalEntryPart> cookingStation = Arrays.asList(
                new TitleJournalPart("Cooking Station"),
                new TextJournalPart("Cooking Station allows you to prepare food. To built it, you need to create two Brick Blocks " +
                        "at an upgraded Stone Station, and also two Cobble Stone Slabs. Once you have these, put the Brick Blocks next " +
                        "to each other, place the Slabs on top of them and right-click the structure while holding a hammer in hand " +
                        "to finish off the process.\n\nCooking Station recipes might require water and heat to operate. You can store " +
                        "some water in the container provided by the station user interface. You might want to use bucket to fill the " +
                        "container.\n\nSome recipes might also require heat. Heat is produced by burning various materials, such as " +
                        "tree logs, coal and charcoal."));
        chapterHandler.registerJournalEntry("cookingStation", cookingStation);

        dependencyMap.put("cookingStation", "introduction");

        List<JournalManager.JournalEntryPart> corn = Arrays.asList(
                new TitleJournalPart("Corn"),
                new TextJournalPart("Corn, also known as maize, is a grain plant. As with any plant fruits gathered, you can use knife to " +
                        "change it into a Corn Sapling, then you can plant it, please keep in mind, that Corn grows best in warm and humid " +
                        "environment.\n\nYou can process Corn for food in two ways, either cook it in water directly at the Cooking " +
                        "Station, or use Quern to grind it into a Corn Flour and cook is with salt to produce bread."));
        chapterHandler.registerJournalEntry("corn", corn);

        dependencyMap.put("corn", "quern");
        dependencyMap.put("corn", "cookingStation");

        List<JournalManager.JournalEntryPart> rice = Arrays.asList(
                new TitleJournalPart("Rice"),
                new TextJournalPart("Rice is a grain plant. As with any plants you gather, you can use knife to change it into a Rice Sapling, " +
                        "then you can plant it, please keep in mind, that Rice requires very humid environment.\n\nTo prepare rice for eating, " +
                        "first you need to grind it in a quern to get White Rice. Once you get it, you can cook it at a Cooking Station with " +
                        "water to produce Cooked Rice."));
        chapterHandler.registerJournalEntry("rice", rice);

        dependencyMap.put("rice", "quern");
        dependencyMap.put("rice", "cookingStation");

        journalManager.registerJournalChapter(chapterId,
            Assets.getTextureRegion("WoodAndStone:journalIcons.FarmingAndCooking"),
            "Farming and Cooking", chapterHandler);
    }

    private void discoveredEntry(EntityRef character, String entryId) {
        for (String dependentOn : dependencyMap.get(entryId)) {
            if (!journalManager.hasEntry(character, chapterId, dependentOn)) {
                discoveredEntry(character, dependentOn);
            }
        }
        if (!journalManager.hasEntry(character, chapterId, entryId)) {
            character.send(new DiscoveredNewJournalEntry(chapterId, entryId));
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef character) {
        Prefab prefab = event.getNewItem().getParentPrefab();
        if (prefab != null) {
            AssetUri prefabUri = prefab.getURI();
            if (prefabUri.equals(new AssetUri(AssetType.PREFAB, "PlantPack", "CornFruit"))) {
                discoveredEntry(character, "corn");
            } else if (prefabUri.equals(new AssetUri(AssetType.PREFAB, "PlantPack", "RiceFruit"))) {
                discoveredEntry(character, "rice");
            }
        }
    }
}
