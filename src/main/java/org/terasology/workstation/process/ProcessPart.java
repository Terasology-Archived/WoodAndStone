/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.workstation.process;

import org.terasology.entitySystem.entity.EntityRef;

import java.util.Set;

public interface ProcessPart {
    /**
     * Validates if this process can be executed, if not InvalidProcessException should be thrown.
     * Returns a list of resultIds used to identify different flavors of executions for this process.
     * List of all resultIds is gathered from all ProcessPart for a process. If null is returned, it is
     * gracefully ignored and processing continues. The selected resultId is then passed to all other methods
     * of this class.
     * If an automated machines gets more than one resultId, it will not starting processing.
     *
     * @param instigator
     * @param workstation
     * @return
     * @throws InvalidProcessException
     */
    public Set<String> validate(EntityRef instigator, EntityRef workstation) throws InvalidProcessException;

    /**
     * Returns duration for this process. All the ProcessParts are queried, and the sum of all results becomes
     * the duration of the process. If the total is 0, the process is executed immediately.
     *
     * @param instigator
     * @param workstation
     * @param result
     * @return
     */
    public long getDuration(EntityRef instigator, EntityRef workstation, String result);

    /**
     * Starts the execution of the process. In this step, for example, all the products, energy and other consumables
     * could be removed from the workstation.
     *
     * @param instigator
     * @param workstation
     * @param result
     */
    public void executeStart(EntityRef instigator, EntityRef workstation, String result);

    /**
     * Finishes the execution of the process. In this step, for example, all the resulting blocks/items could be
     * placed in the workstation.
     *
     * @param instigator
     * @param workstation
     * @param result
     */
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result);
}
