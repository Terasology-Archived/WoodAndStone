package org.terasology.journal;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeEntityCreated;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class JournalAuthoritySystem implements ComponentSystem {
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void addJournalAccessComponentToPlayers(BeforeEntityCreated event, EntityRef character) {
        if (event.getPrefab() != null && event.getPrefab().equals("engine:player")) {
            JournalAccessComponent journalAccess = new JournalAccessComponent();
            journalAccess.discoveredJournalEntries = new LinkedHashMap<>();
            event.addComponent(journalAccess);
        }
    }

    @ReceiveEvent(components = {JournalAccessComponent.class})
    public void newJournalEntryDiscovered(DiscoveredNewJournalEntry event, EntityRef character) {
        // Apply the changes to the server object
        JournalAccessComponent journalAccess = character.getComponent(JournalAccessComponent.class);
        String chapterId = event.getChapterId();
        List<String> entries = journalAccess.discoveredJournalEntries.get(chapterId);
        if (entries == null) {
            entries = new LinkedList<>();
            journalAccess.discoveredJournalEntries.put(chapterId, entries);
        }
        entries.add(event.getEntryId());
        character.saveComponent(journalAccess);

        // Notify the client
        character.send(new NewJournalEntryDiscoveredEvent(chapterId, event.getEntryId()));
    }
}
