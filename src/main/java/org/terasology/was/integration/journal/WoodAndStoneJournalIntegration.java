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
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.multiBlock.MultiBlockFormed;
import org.terasology.registry.In;
import org.terasology.world.block.Block;

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

    private String chapterId = "WoodAndStone";

    @Override
    public void initialise() {
        StaticJournalChapterHandler chapterHandler = new StaticJournalChapterHandler();

        Prefab stoneItem = prefabManager.getPrefab("WoodAndStone:Stone");
        Prefab toolStoneItem = prefabManager.getPrefab("WoodAndStone:ToolStone");
        Prefab axeHammerHeadItem = prefabManager.getPrefab("WoodAndStone:AxeHammerHead");
        Prefab stickItem = prefabManager.getPrefab("WoodAndStone:Stick");
        Prefab twigItem = prefabManager.getPrefab("WoodAndStone:Twig");
        Prefab crudeAxeHammerItem = prefabManager.getPrefab("WoodAndStone:CrudeAxeHammer");

        List<JournalManager.JournalEntryPart> firstEntry = Arrays.asList(
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

        chapterHandler.registerJournalEntry("6", true, "Now! I should be able to sharpen a stone and use it to attach another " +
                "stone directly to a stick to make a hammer without binding it with twigs to strengthen the connection. " +
                "I can use the same technique to create other tools. These stones are much more sturdy than the ones I've been using so far.");

        journalManager.registerJournalChapter(chapterId, Assets.getTexture("WoodAndStone:WoodAndStoneJournal"), "Wood and Stone",
                chapterHandler);
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
