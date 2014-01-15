package org.terasology.journal;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.Share;
import org.terasology.rendering.assets.texture.Texture;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(JournalManager.class)
public class JournalManagerImpl implements ComponentSystem, JournalManager {
    private Map<String, JournalChapter> journalChapters = new LinkedHashMap<>();
    private Map<String, Map<String, String>> journalEntries = new HashMap<>();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void registerJournalChapter(String chapterId, Texture icon, String name) {
        journalChapters.put(chapterId, new JournalChapter(icon, name));
    }

    @Override
    public void registerJournalEntry(String chapterId, String entryId, String text) {
        if (!journalChapters.containsKey(chapterId))
            throw new IllegalStateException("Unable to add entry to an unknown chapter");
        Map<String, String> chapterEntries = journalEntries.get(chapterId);
        if (chapterEntries == null) {
            chapterEntries = new LinkedHashMap<>();
            journalEntries.put(chapterId, chapterEntries);
        }
        chapterEntries.put(entryId, text);
    }

    @Override
    public boolean hasEntry(EntityRef player, String chapterId, String entryId) {
        JournalAccessComponent journal = player.getComponent(JournalAccessComponent.class);
        List<String> entryIds = journal.discoveredJournalEntries.get(chapterId);
        return entryIds != null && entryIds.contains(entryId);
    }

    public Map<String, List<String>> getEntriesForPlayer(EntityRef player) {
        JournalAccessComponent journal = player.getComponent(JournalAccessComponent.class);
        return journal.discoveredJournalEntries;
    }

    private class JournalChapter {
        private Texture texture;
        private String name;

        private JournalChapter(Texture texture, String name) {
            this.texture = texture;
            this.name = name;
        }
    }
}
