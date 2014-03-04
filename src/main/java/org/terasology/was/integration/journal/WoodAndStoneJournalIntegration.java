/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.was.integration.journal;

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
import org.terasology.journal.part.TextJournalPart;
import org.terasology.journal.part.TimestampJournalPart;
import org.terasology.journal.part.TitleJournalPart;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.multiBlock.MultiBlockFormed;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class WoodAndStoneJournalIntegration extends BaseComponentSystem {
    @In
    private JournalManager journalManager;
    @In
    private PrefabManager prefabManager;
    @In
    private BlockManager blockManager;

    private String chapterId = "WoodAndStone";

    @Override
    public void preBegin() {
        StaticJournalChapterHandler chapterHandler = new StaticJournalChapterHandler();

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

        List<JournalManager.JournalEntryPart> firstEntry = Arrays.asList(
                new TitleJournalPart("Wood and Stone"),
                new TextJournalPart("Where am I? How did I get here? ...\nWhat am I going to do now? ...\n" +
                        "How am I going to survive the night? ...\n\nI should probably start off with building a safe shelter. " +
                        "I need some tools for that.\n\nI should get some sticks from the nearby tree branches and dig in the ground for some " +
                        "stones I might have a use for.\n\nWhile I'm at it, I will probably need something to bind the stick and stone together - " +
                        "twigs, should be good for that.\n\nOnce I get two stones, I should be able to make a Tool Stone (press G to open crafting window)."),
                new RecipeJournalPart(new Block[2], new Prefab[]{stoneItem, stoneItem}, null, toolStoneItem, 1),
                new TextJournalPart("Once I get the Tool Stone, by using the Tool Stone on another stone I should be able " +
                        "to make an Axe-Hammer Head."),
                new RecipeJournalPart(new Block[2], new Prefab[]{toolStoneItem, stoneItem}, null, axeHammerHeadItem, 1),
                new TextJournalPart("Then I can combine the Axe-Hammer Head with a Stick and a Twig to create a Crude Axe-Hammer."),
                new RecipeJournalPart(new Block[3], new Prefab[]{axeHammerHeadItem, stickItem, twigItem}, null, crudeAxeHammerItem, 1));

        chapterHandler.registerJournalEntry("1", firstEntry);

        chapterHandler.registerJournalEntry("2", true, "Excellent! I got the Axe-Hammer, I should be able to cut some of the trees with it. " +
                "I can also use it to dig stone to get some more Stones for my crafting. It's not perfect but will have to do until I get my hands on a " +
                "better hammer or a pick.");

        chapterHandler.registerJournalEntry("3", true, "These are big, there is no way I could handle them in my hands. " +
                "I have to build a place where I could work on them. I should place two of the logs on the ground next to each other " +
                "and then place my Axe on it (right-click your Axe-Hammer on the top face of one of the logs).");

        chapterHandler.registerJournalEntry("4", true, "Now I can work on the logs (to open the interface, press 'E' while pointing " +
                "on the station).\n\nI can store some ingredients in the left-top corner of the station and my axe in the bottom-center " +
                "of the station. It's very crude and won't let me do much, but once I gather 10 Wood Planks I should be able to upgrade it. " +
                "(to upgrade place the ingredients into lower-left corner of the interface and press the 'Upgrade' button)");

        chapterHandler.registerJournalEntry("5", true, "Finally I can make something more useful than just planks. " +
                "Not only that, but I can also create planks more efficiently! Quality of the workspace speaks for itself.\n\n" +
                "But I still can't make any tools. Hard to make it just out of wood, haha. I should probably find a good place " +
                "to work on stone materials. I should make two tables using planks and sticks.\n\nOnce I get the tables I should place them " +
                "on the ground next to each other and put my Axe-Hammer on top of one of them (same as before).");

        List<JournalManager.JournalEntryPart> stoneHammer = Arrays.asList(
                new TimestampJournalPart(),
                new TextJournalPart("Now! On this workstation I should be able to create more durable tools. " +
                        "I should get myself a couple of hammers and finally go mining!"),
                new RecipeJournalPart(new Block[3], new Prefab[]{stoneItem, twigItem, stickItem}, null, stoneHammerItem, 1),
                new TextJournalPart("It is going to be dark out there in the mines, I should prepare some torches in advance. " +
                        "I can use some of the Resin found while cutting trees with stick in a crafting window (press G) to " +
                        "create Unlit Torches."),
                new RecipeJournalPart(new Block[2], new Prefab[]{resinItem, stickItem}, null, unlitTorchItem, 1),
                new TextJournalPart("Once I get them I should be able to light them up using flint in a crafting window. " +
                        "Just need to make sure not to light too many of them, as the torches last only for a bit of time."),
                new RecipeJournalPart(new Block[2], new Prefab[]{unlitTorchItem, flintItem}, litTorchBlock, null, 1));
        chapterHandler.registerJournalEntry("6", stoneHammer);

        journalManager.registerJournalChapter(chapterId,
            Assets.getTextureRegion("WoodAndStone:journalIcons.WoodAndStone"),
            "Wood and Stone", chapterHandler);
    }

    @ReceiveEvent
    public void playerSpawned(OnPlayerSpawnedEvent event, EntityRef player) {
        player.send(new DiscoveredNewJournalEntry(chapterId, "1"));
    }

    @ReceiveEvent(components = {CraftingStationComponent.class})
    public void craftingStationFormed(MultiBlockFormed craftingStationFormed, EntityRef station) {
        EntityRef character = craftingStationFormed.getInstigator();
        String workstationType = station.getComponent(CraftingStationComponent.class).type;
        if (workstationType.equals("WoodAndStone:BasicWoodcrafting")
                && !journalManager.hasEntry(character, chapterId, "4")) {
            character.send(new DiscoveredNewJournalEntry(chapterId, "4"));
        } else if (workstationType.equals("WoodAndStone:BasicStonecrafting") && !journalManager.hasEntry(character, chapterId, "6")) {
            character.send(new DiscoveredNewJournalEntry(chapterId, "6"));
        }
    }

    @ReceiveEvent
    public void craftingStationUpgraded(CraftingStationUpgraded craftingStationUpgraded, EntityRef character) {
        if (craftingStationUpgraded.getCraftingStation().getComponent(CraftingStationComponent.class).type.equals("WoodAndStone:StandardWoodcrafting")
                && !journalManager.hasEntry(character, chapterId, "5")) {
            character.send(new DiscoveredNewJournalEntry(chapterId, "5"));
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef character) {
        CraftingStationToolComponent toolComponent = event.getNewItem().getComponent(CraftingStationToolComponent.class);
        CraftingStationIngredientComponent ingredientComponent = event.getNewItem().getComponent(CraftingStationIngredientComponent.class);
        if (toolComponent != null) {
            List<String> toolTypes = toolComponent.type;
            if (toolTypes.contains("hammer") && toolTypes.contains("axe") && !journalManager.hasEntry(character, chapterId, "2")) {
                character.send(new DiscoveredNewJournalEntry(chapterId, "2"));
            }
        }
        if (ingredientComponent != null) {
            String ingredientType = ingredientComponent.type;
            if (ingredientType.equals("WoodAndStone:wood") && !journalManager.hasEntry(character, chapterId, "3")) {
                character.send(new DiscoveredNewJournalEntry(chapterId, "3"));
            }
        }
    }

}
