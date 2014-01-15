package org.terasology.journal;

import org.terasology.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class DiscoveredNewJournalEntry implements Event {
    private String chapterId;
    private String entryId;

    public DiscoveredNewJournalEntry(String chapterId, String entryId) {
        this.chapterId = chapterId;
        this.entryId = entryId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public String getEntryId() {
        return entryId;
    }
}
