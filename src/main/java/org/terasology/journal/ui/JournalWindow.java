package org.terasology.journal.ui;

import org.terasology.rendering.gui.widgets.UIWindow;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class JournalWindow extends UIWindow {
    public JournalWindow() {
        setId("Journal:Journal");
        setModal(true);
        maximize();
    }
}
