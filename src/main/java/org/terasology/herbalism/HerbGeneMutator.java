package org.terasology.herbalism;

import com.google.common.base.Predicate;
import org.terasology.genome.breed.mutator.VocabularyGeneMutator;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class HerbGeneMutator extends VocabularyGeneMutator {
    public HerbGeneMutator() {
        super("ABCD",
                new Predicate<Integer>() {
                    @Override
                    public boolean apply(Integer input) {
                        return input != 0;
                    }
                });
    }
}
