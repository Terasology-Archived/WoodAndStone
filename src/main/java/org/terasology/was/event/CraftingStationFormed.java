package org.terasology.was.event;

import org.terasology.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationFormed implements Event {
    private String workstationType;

    public CraftingStationFormed(String workstationType) {
        this.workstationType = workstationType;
    }

    public String getWorkstationType() {
        return workstationType;
    }
}
