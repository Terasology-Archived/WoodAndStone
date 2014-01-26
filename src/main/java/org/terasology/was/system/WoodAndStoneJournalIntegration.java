package org.terasology.was.system;

import org.terasology.crafting.component.CraftInHandRecipeComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalManager;
import org.terasology.logic.inventory.PickedUpItem;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.was.event.CraftingStationFormed;
import org.terasology.workstation.component.CraftingStationComponent;
import org.terasology.workstation.component.CraftingStationIngredientComponent;
import org.terasology.workstation.event.CraftingStationUpgraded;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class WoodAndStoneJournalIntegration implements ComponentSystem {
    @In
    private JournalManager journalManager;

    private String chapterId = "WoodAndStone";

    @Override
    public void initialise() {
        journalManager.registerJournalChapter(chapterId, null, "Wood and Stone");

        journalManager.registerJournalEntry(chapterId, "1", "Where am I? How did I get here? ...\nWhat am I going to do now? ...\n" +
                "How am I going to survive the night? ...\n\nI should probably start off with building a safe shelter. " +
                "I need some tools for that.\n\nI should get some sticks from the nearby tree branches and dig in the ground for some " +
                "stones I might have a use for.\n\nWhile I'm at it, I will probably need something to bind the stick and stone together - " +
                "plant fibres, also from branches, should be good for that.\n\nOnce I get a Stick, Stone and a Plant Fibre, I will be able " +
                "to make a Crude Hammer (press G to open crafting window).");

        journalManager.registerJournalEntry(chapterId, "2", "Excellent! I got a hammer, I should be able to sharpen one of the stones " +
                "and using the same technique as for the hammer, create a Crude Hammer! I can also dig stone with it to get stones more " +
                "easily. It's not perfect but will have to do until I get my hands on a pick.");

        journalManager.registerJournalEntry(chapterId, "3", "Ouch! That thing is sharp! I should watch out when handling it. " +
                "Now I should be able to cut some of the trees and get Wood Logs.");

        journalManager.registerJournalEntry(chapterId, "4", "These are big, there is no way I could handle them in my hands. " +
                "I have to build a place where I could work on them. I should place two of the logs on the ground next to each other " +
                "and then place my Axe on it (right-click your axe on the top face of one of the logs).");

        journalManager.registerJournalEntry(chapterId, "5", "Now I can work on the logs (to open the interface, press 'E' while pointing " +
                "on the station).\n\nI can store some ingredients in the left-top corner of the station and my axe in the bottom-center " +
                "of the station. It's very crude and won't let me do much, but once I gather 10 Wood Planks I should be able to upgrade it. " +
                "(to upgrade place the ingredients into lower-left corner of the interface and press the 'Upgrade' button)");

        journalManager.registerJournalEntry(chapterId, "6", "Finally I can make something more useful than just planks. " +
                "Not only that, but I can also create planks more efficiently! Quality of the workspace speaks for itself.\n\n" +
                "But I still can't make any tools. Hard to make it just out of wood, haha. I should probably find a good place " +
                "to work on stone materials. I should make two tables using planks and sticks, but axe is not enough to do that, " +
                "I will also need hammer (place it next to your axe in the station).\n\nOnce I get the tables I should place them " +
                "on the ground next to each other and put my hammer on top of one of them (same as with axe before).");

        journalManager.registerJournalEntry(chapterId, "7", "Now! I can start making the tools, and I don't even need Plant Fibres " +
                "for them, just a stick and a couple of stones, including sharp-edged ones.");
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void playerSpawned(OnPlayerSpawnedEvent event, EntityRef player) {
        player.send(new DiscoveredNewJournalEntry(chapterId, "1"));
    }

    @ReceiveEvent
    public void craftingStationFormed(CraftingStationFormed craftingStationFormed, EntityRef character) {
        String workstationType = craftingStationFormed.getWorkstationType();
        if (workstationType.equals("WoodAndStone:BasicWoodcrafting")
                && !journalManager.hasEntry(character, chapterId, "5")) {
            character.send(new DiscoveredNewJournalEntry(chapterId, "5"));
        } else if (workstationType.equals("WoodAndStone:BasicStonecrafting") && !journalManager.hasEntry(character, chapterId, "7")) {
            character.send(new DiscoveredNewJournalEntry(chapterId, "7"));
        }
    }

    @ReceiveEvent
    public void craftingStationUpgraded(CraftingStationUpgraded craftingStationUpgraded, EntityRef character) {
        if (craftingStationUpgraded.getCraftingStation().getComponent(CraftingStationComponent.class).type.equals("WoodAndStone:StandardWoodcrafting")
                && !journalManager.hasEntry(character, chapterId, "6")) {
            character.send(new DiscoveredNewJournalEntry(chapterId, "6"));
        }
    }

    @ReceiveEvent
    public void playerPickedUpItem(PickedUpItem event, EntityRef character) {
        CraftInHandRecipeComponent handRecipeComponent = event.getItem().getComponent(CraftInHandRecipeComponent.class);
        CraftingStationIngredientComponent ingredientComponent = event.getItem().getComponent(CraftingStationIngredientComponent.class);
        if (handRecipeComponent != null) {
            String componentType = handRecipeComponent.componentType;
            if (componentType.equals("hammer") && !journalManager.hasEntry(character, chapterId, "2")) {
                character.send(new DiscoveredNewJournalEntry(chapterId, "2"));
            } else if (componentType.equals("axe") && !journalManager.hasEntry(character, chapterId, "3")) {
                character.send(new DiscoveredNewJournalEntry(chapterId, "3"));
            }
        }
        if (ingredientComponent != null) {
            String ingredientType = ingredientComponent.type;
            if (ingredientType.equals("WoodAndStone:wood") && !journalManager.hasEntry(character, chapterId, "4")) {
                character.send(new DiscoveredNewJournalEntry(chapterId, "4"));
            }
        }
    }

}
