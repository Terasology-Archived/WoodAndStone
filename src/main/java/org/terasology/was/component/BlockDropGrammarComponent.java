package org.terasology.was.component;

import org.terasology.entitySystem.Component;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockDropGrammarComponent implements Component {
    public List<String> blockDrops;
    public List<String> itemDrops;
}
