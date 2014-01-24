/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.anotherWorld.util;

import org.terasology.utilities.random.Random;

import java.util.LinkedList;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ChanceRandomizer<T> {
    private int granularity;

    private float chanceSum;
    private LinkedList<ObjectChance<T>> objectChances = new LinkedList<>();

    private Object[] lookupArray;

    public ChanceRandomizer(int granularity) {
        this.granularity = granularity;
        lookupArray = new Object[granularity];
    }

    public void addChance(float chance, T object) {
        objectChances.add(new ObjectChance<T>(chance, object));
        chanceSum += chance;
    }

    public void initialize() {
        int index = 0;
        for (ObjectChance<T> objectChance : objectChances) {
            int maxIndex = index + ((int) ((objectChance.chance / chanceSum) * granularity));
            for (int i = index; i < maxIndex; i++) {
                lookupArray[i] = objectChance.object;
            }
            index = maxIndex;
        }
        if (index < granularity && !objectChances.isEmpty()) {
            for (int i = index; i < granularity; i++) {
                lookupArray[i] = objectChances.getLast().object;
            }
        }
    }

    public T randomizeObject(Random random) {
        return (T) lookupArray[random.nextInt(granularity)];
    }

    private static class ObjectChance<T> {
        private float chance;
        private T object;

        private ObjectChance(float chance, T object) {
            this.chance = chance;
            this.object = object;
        }
    }
}
