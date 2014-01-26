package org.terasology.multiBlock;

import org.terasology.logic.common.ActivateEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface ActivateEventFilter {
    public boolean accepts(ActivateEvent event);
}
