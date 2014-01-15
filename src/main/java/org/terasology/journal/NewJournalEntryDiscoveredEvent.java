package org.terasology.journal;

import org.terasology.network.NetworkEvent;
import org.terasology.network.OwnerEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@OwnerEvent
public class NewJournalEntryDiscoveredEvent extends NetworkEvent {
    private String chapterId;
    private String entryId;

    public NewJournalEntryDiscoveredEvent() {
    }

    public NewJournalEntryDiscoveredEvent(String chapterId, String entryId) {
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
