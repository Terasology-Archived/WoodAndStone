package org.terasology.journal;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.journal.ui.NewEntryWindow;
import org.terasology.logic.manager.GUIManager;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class JournalClientSystem implements ComponentSystem {
    @In
    private GUIManager guiManager;

    @Override
    public void initialise() {
        guiManager.registerWindow("Journal:NewEntry", NewEntryWindow.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void newEntryNotificationReceived(NewJournalEntryDiscoveredEvent event, EntityRef character) {
        guiManager.openWindow("Journal:NewEntry");
    }
}
