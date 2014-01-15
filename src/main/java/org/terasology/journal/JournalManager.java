package org.terasology.journal;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.assets.texture.Texture;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface JournalManager {
    public void registerJournalChapter(String chapterId, Texture icon, String name);

    public void registerJournalEntry(String chapterId, String entryId, String text);

    public boolean hasEntry(EntityRef player, String chapterId, String entryId);
}
