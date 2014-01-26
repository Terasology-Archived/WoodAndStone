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

/**
 * Alpha function translates one value into another. The input value is more-or-less in range of 0-1, output should
 * be in the same range.
 * See http://easings.net/ for more details and examples.
 * In generators used mainly for modifying the uniform noise function. This allows to transform a uniform 0-1 space
 * of possible values into more controlled one, for example - allows to modify so that low values appear more often
 * than higher values.
 *
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface AlphaFunction {
    public float execute(float value);
}
