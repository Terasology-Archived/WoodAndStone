package org.terasology.journal;

import org.terasology.entitySystem.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class JournalAccessComponent implements Component {
    public Map<String, List<String>> discoveredJournalEntries;
}
