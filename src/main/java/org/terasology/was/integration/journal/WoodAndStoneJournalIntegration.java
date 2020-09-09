// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.integration.journal;

import org.terasology.crafting.component.CraftingStationComponent;
import org.terasology.crafting.component.CraftingStationIngredientComponent;
import org.terasology.crafting.component.CraftingStationToolComponent;
import org.terasology.crafting.event.CraftingStationUpgraded;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.HTMLLikeParser;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.inventory.logic.events.InventorySlotChangedEvent;
import org.terasology.journal.BrowserJournalChapterHandler;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalEntryProducer;
import org.terasology.journal.JournalManager;
import org.terasology.journal.TimestampResolver;
import org.terasology.multiBlock.MultiBlockFormed;
import org.terasology.nui.HorizontalAlign;
import org.terasology.seasons.SeasonSystem;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class WoodAndStoneJournalIntegration extends BaseComponentSystem {
    private final String wasChapterId = "WoodAndStone";
    private final String seasonsChapterId = "Seasons";
    private final ParagraphRenderStyle centerRenderStyle = new ParagraphRenderStyle() {
        @Override
        public HorizontalAlign getHorizontalAlignment() {
            return HorizontalAlign.CENTER;
        }
    };
    @In
    private JournalManager journalManager;
    @In
    private PrefabManager prefabManager;
    @In
    private BlockManager blockManager;
    @In
    private SeasonSystem seasonSystem;

    @Override
    public void preBegin() {
        createSeasonsChapter();

        BrowserJournalChapterHandler chapterHandler = new BrowserJournalChapterHandler();

        Prefab stoneItem = prefabManager.getPrefab("WoodAndStone:Stone");
        Prefab toolStoneItem = prefabManager.getPrefab("WoodAndStone:ToolStone");
        Prefab axeHammerHeadItem = prefabManager.getPrefab("WoodAndStone:AxeHammerHead");
        Prefab stickItem = prefabManager.getPrefab("WoodAndStone:Stick");
        Prefab twigItem = prefabManager.getPrefab("WoodAndStone:Twig");
        Prefab resinItem = prefabManager.getPrefab("WoodAndStone:Resin");
        Prefab unlitTorchItem = prefabManager.getPrefab("WoodAndStone:UnlitTorch");
        Prefab flintItem = prefabManager.getPrefab("WoodAndStone:Flint");

        Prefab crudeAxeHammerItem = prefabManager.getPrefab("WoodAndStone:CrudeAxeHammer");
        Prefab stoneHammerItem = prefabManager.getPrefab("WoodAndStone:StoneHammer");

        Block litTorchBlock = blockManager.getBlockFamily("WoodAndStone:LitTorch").getArchetypeBlock();

        chapterHandler.registerJournalEntry("1",
                Arrays.asList(
                        createTitleParagraph("Wood and Stone"),
                        createTextParagraph("Where am I? How did I get here? ...<l>What am I going to do now? ...<l>" +
                                "How am I going to survive the night? ...<l><l>I should probably start off with " +
                                "building a safe shelter. " +
                                "I need some tools for that.<l><l>I should get some sticks from the nearby tree " +
                                "branches and dig in the ground for some " +
                                "stones I might have a use for.<l><l>While I'm at it, I will probably need something " +
                                "to bind the stick and stone together - " +
                                "twigs, should be good for that.<l><l>Once I get two stones, I should be able to make" +
                                " a Tool Stone (press G to open crafting window)."),
                        new RecipeParagraph(new Block[2], new Prefab[]{stoneItem, stoneItem}, null, toolStoneItem, 1),
                        createTextParagraph("Once I get the Tool Stone, by using the Tool Stone on another stone I " +
                                "should be able " +
                                "to make an Axe-Hammer Head."),
                        new RecipeParagraph(new Block[2], new Prefab[]{toolStoneItem, stoneItem}, null,
                                axeHammerHeadItem, 1),
                        createTextParagraph("Then I can combine the Axe-Hammer Head with a Stick and a Twig to create" +
                                " a Crude Axe-Hammer."),
                        new RecipeParagraph(new Block[3], new Prefab[]{axeHammerHeadItem, stickItem, twigItem}, null,
                                crudeAxeHammerItem, 1)
                ));

        chapterHandler.registerJournalEntry("2",
                createTimestampEntryProducer("Excellent! I got the Axe-Hammer, I should be able to cut some of the " +
                        "trees with it. " +
                        "I can also use it to dig stone to get some more Stones for my crafting. It's not perfect but" +
                        " will have to do until I get my hands on a " +
                        "better hammer or a pick."));

        chapterHandler.registerJournalEntry("3",
                createTimestampEntryProducer("These are big, there is no way I could handle them in my hands. " +
                        "I have to build a place where I could work on them. I should place two of the logs on the " +
                        "ground next to each other " +
                        "and then place my Axe on it (right-click your Axe-Hammer on the top face of one of the logs)" +
                        "."));

        chapterHandler.registerJournalEntry("4",
                createTimestampEntryProducer("Now I can work on the logs (to open the interface, press 'E' while " +
                        "pointing " +
                        "on the station).<l><l>I can store some ingredients in the left-top corner of the station and" +
                        " my axe in the bottom-center " +
                        "of the station. It's very crude and won't let me do much, but once I gather 10 Wood Planks I" +
                        " should be able to upgrade it. " +
                        "(to upgrade place the ingredients into lower-left corner of the interface and press the " +
                        "'Upgrade' button)"));

        chapterHandler.registerJournalEntry("5",
                createTimestampEntryProducer("Finally I can make something more useful than just planks. " +
                        "Not only that, but I can also create planks more efficiently! Quality of the workspace " +
                        "speaks for itself.<l><l>" +
                        "But I still can't make any tools. Hard to make it just out of wood, haha. I should probably " +
                        "find a good place " +
                        "to work on stone materials. I should make two tables using planks and sticks.<l><l>Once I " +
                        "get the tables I should place them " +
                        "on the ground next to each other and put my Axe-Hammer on top of one of them (same as " +
                        "before)."));

        chapterHandler.registerJournalEntry("6",
                new JournalEntryProducer() {
                    @Override
                    public Collection<ParagraphData> produceParagraph(long date) {
                        return Arrays.asList(
                                HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle,
                                        TimestampResolver.getJournalEntryDate(date)),
                                createTextParagraph("Now! On this workstation I should be able to create more durable" +
                                        " tools. " +
                                        "I should get myself a couple of hammers and finally go mining!"),
                                new RecipeParagraph(new Block[3], new Prefab[]{stoneItem, twigItem, stickItem}, null,
                                        stoneHammerItem, 1),
                                createTextParagraph("It is going to be dark out there in the mines, I should prepare " +
                                        "some torches in advance. " +
                                        "I can use some of the Resin found while cutting trees with stick in a " +
                                        "crafting window (press G) to " +
                                        "create Unlit Torches."),
                                new RecipeParagraph(new Block[2], new Prefab[]{resinItem, stickItem}, null,
                                        unlitTorchItem, 1),
                                createTextParagraph("Once I get them I should be able to light them up using flint in" +
                                        " a crafting window. " +
                                        "Just need to make sure not to light too many of them, as the torches last " +
                                        "only for a bit of time."),
                                new RecipeParagraph(new Block[2], new Prefab[]{unlitTorchItem, flintItem},
                                        litTorchBlock, null, 1)
                        );
                    }
                });

        journalManager.registerJournalChapter(wasChapterId,
                Assets.getTextureRegion("WoodAndStone:journalIcons#WoodAndStone").get(),
                "Wood and Stone", chapterHandler);
    }

    private JournalEntryProducer createTimestampEntryProducer(String text) {
        return new JournalEntryProducer() {
            @Override
            public Collection<ParagraphData> produceParagraph(long date) {
                return Arrays.asList(
                        HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle,
                                TimestampResolver.getJournalEntryDate(date)),
                        HTMLLikeParser.parseHTMLLikeParagraph(null,
                                text));
            }
        };
    }

    private ParagraphData createTextParagraph(String text) {
        return HTMLLikeParser.parseHTMLLikeParagraph(null, text);
    }

    private ParagraphData createTitleParagraph(String title) {
        return HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle,
                "<f engine:NotoSans-Regular-Title>" + title + "</f>");
    }

    private void createSeasonsChapter() {
        BrowserJournalChapterHandler chapterHandler = new BrowserJournalChapterHandler();

        chapterHandler.registerJournalEntry("1",
                new JournalEntryProducer() {
                    @Override
                    public Collection<ParagraphData> produceParagraph(long date) {
                        return Arrays.asList(
                                createTitleParagraph("Time of Year"),
                                HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle,
                                        "It is " + seasonSystem.getSeasonDayDescription())
                        );
                    }
                });

        journalManager.registerJournalChapter(seasonsChapterId,
                Assets.getTextureRegion("WoodAndStone:journalIcons#WoodAndStone").get(),
                "Seasons", chapterHandler);
    }

    @ReceiveEvent
    public void playerSpawned(OnPlayerSpawnedEvent event, EntityRef player) {
        player.send(new DiscoveredNewJournalEntry(seasonsChapterId, "1"));
        player.send(new DiscoveredNewJournalEntry(wasChapterId, "1"));
    }

    @ReceiveEvent
    public void craftingStationFormed(MultiBlockFormed craftingStationFormed, EntityRef station,
                                      CraftingStationComponent craftingStationComponent) {
        EntityRef character = craftingStationFormed.getInstigator();
        String workstationType = craftingStationComponent.type;
        if (workstationType.equals("WoodAndStone:BasicWoodcrafting")
                && !journalManager.hasEntry(character, wasChapterId, "4")) {
            character.send(new DiscoveredNewJournalEntry(wasChapterId, "4"));
        } else if (workstationType.equals("WoodAndStone:BasicStonecrafting") && !journalManager.hasEntry(character,
                wasChapterId, "6")) {
            character.send(new DiscoveredNewJournalEntry(wasChapterId, "6"));
        }
    }

    @ReceiveEvent
    public void craftingStationUpgraded(CraftingStationUpgraded craftingStationUpgraded, EntityRef character) {
        if (craftingStationUpgraded.getCraftingStation().getComponent(CraftingStationComponent.class).type.equals(
                "WoodAndStone:StandardWoodcrafting")
                && !journalManager.hasEntry(character, wasChapterId, "5")) {
            character.send(new DiscoveredNewJournalEntry(wasChapterId, "5"));
        }
    }

    @ReceiveEvent
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef character,
                                   CharacterComponent characterComponent) {
        CraftingStationToolComponent toolComponent =
                event.getNewItem().getComponent(CraftingStationToolComponent.class);
        CraftingStationIngredientComponent ingredientComponent =
                event.getNewItem().getComponent(CraftingStationIngredientComponent.class);
        if (toolComponent != null) {
            List<String> toolTypes = toolComponent.type;
            if (toolTypes.contains("hammer") && toolTypes.contains("axe") && !journalManager.hasEntry(character,
                    wasChapterId, "2")) {
                character.send(new DiscoveredNewJournalEntry(wasChapterId, "2"));
            }
        }
        if (ingredientComponent != null) {
            String ingredientType = ingredientComponent.type;
            if (ingredientType.equals("WoodAndStone:wood") && !journalManager.hasEntry(character, wasChapterId, "3")) {
                character.send(new DiscoveredNewJournalEntry(wasChapterId, "3"));
            }
        }
    }

}
