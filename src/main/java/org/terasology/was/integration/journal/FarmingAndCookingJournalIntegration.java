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
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.HTMLLikeParser;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.journal.BrowserJournalChapterHandler;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalManager;
import org.terasology.nui.HorizontalAlign;

import java.util.Arrays;

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
        BrowserJournalChapterHandler chapterHandler = new BrowserJournalChapterHandler();

        Prefab stoneItem = prefabManager.getPrefab("WoodAndStone:stone");
        Prefab stickItem = prefabManager.getPrefab("WoodAndStone:stick");

        Block quernBlock = blockManager.getBlockFamily("WoodAndStone:quern").getArchetypeBlock();

        chapterHandler.registerJournalEntry("introduction",
                Arrays.asList(
                        createTitleParagraph("Introduction"),
                        createTextParagraph("This part of your journal will guide you through farming and cooking activities required " +
                                "to survive in Throughout the Ages world.")
                ));

        chapterHandler.registerJournalEntry("quern",
                Arrays.asList(
                        createTitleParagraph("Quern"),
                        createTextParagraph("Quern, also known as a hand mill, allows you to grind various substances, including grain. " +
                                "It is also useful for grinding crystals into dust for easier metal extraction.<l><l>To craft a " +
                                "quern you need six Stones and two Sticks."),
                        new RecipeParagraph(new Block[2], new Prefab[]{stoneItem, stickItem}, quernBlock, null, 1),
                        createTextParagraph("Using a hammer you can then create a Quern at the stone working station.")
                ));

        dependencyMap.put("quern", "introduction");

        chapterHandler.registerJournalEntry("cookingStation",
                Arrays.asList(
                        createTitleParagraph("Cooking Station"),
                        createTextParagraph("Cooking Station allows you to prepare food. To built it, you need to create two Brick Blocks " +
                                "at an upgraded Stone Station, and also two Cobble Stone Slabs. Once you have these, put the Brick Blocks next " +
                                "to each other, place the Slabs on top of them and right-click the structure while holding a hammer in hand " +
                                "to finish off the process.<l><l>Cooking Station recipes might require water and heat to operate. You can store " +
                                "some water in the container provided by the station user interface. You might want to use bucket to fill the " +
                                "container.<l><l>Some recipes might also require heat. Heat is produced by burning various materials, such as " +
                                "tree logs, coal and charcoal.")
                ));

        dependencyMap.put("cookingStation", "introduction");

        chapterHandler.registerJournalEntry("corn",
                Arrays.asList(
                        createTitleParagraph("Corn"),
                        createTextParagraph("Corn, also known as maize, is a grain plant. As with any plant fruits gathered, you can use knife to " +
                                "change it into a Corn Sapling, then you can plant it, please keep in mind, that Corn grows best in warm and humid " +
                                "environment.<l><l>You can process Corn for food in two ways, either cook it in water directly at the Cooking " +
                                "Station, or use Quern to grind it into a Corn Flour and cook is with salt to produce bread.")
                ));

        dependencyMap.put("corn", "quern");
        dependencyMap.put("corn", "cookingStation");

        chapterHandler.registerJournalEntry("rice",
                Arrays.asList(
                        createTitleParagraph("Rice"),
                        createTextParagraph("Rice is a grain plant. As with any plants you gather, you can use knife to change it into a Rice Sapling, " +
                                "then you can plant it, please keep in mind, that Rice requires very humid environment.<l><l>To prepare rice for eating, " +
                                "first you need to grind it in a quern to get White Rice. Once you get it, you can cook it at a Cooking Station with " +
                                "water to produce Cooked Rice.")
                ));

        dependencyMap.put("rice", "quern");
        dependencyMap.put("rice", "cookingStation");

        journalManager.registerJournalChapter(chapterId,
                Assets.getTextureRegion("WoodAndStone:journalIcons#FarmingAndCooking").get(),
                "Farming and Cooking", chapterHandler);
    }

    private ParagraphData createTextParagraph(String text) {
        return HTMLLikeParser.parseHTMLLikeParagraph(null, text);
    }

    private ParagraphData createTitleParagraph(String title) {
        return HTMLLikeParser.parseHTMLLikeParagraph(
                new ParagraphRenderStyle() {
                    @Override
                    public HorizontalAlign getHorizontalAlignment() {
                        return HorizontalAlign.CENTER;
                    }
                }, "<f engine:NotoSans-Regular-Title>" + title + "</f>");
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

    @ReceiveEvent
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef character,
                                   CharacterComponent characterComponent) {
        Prefab prefab = event.getNewItem().getParentPrefab();
        if (prefab != null) {
            ResourceUrn prefabUri = prefab.getUrn();
            if (prefabUri.equals(new ResourceUrn("AnotherWorldPlants", "CornFruit"))) {
                discoveredEntry(character, "corn");
            } else if (prefabUri.equals(new ResourceUrn("AnotherWorldPlants", "RiceFruit"))) {
                discoveredEntry(character, "rice");
            }
        }
    }
}
