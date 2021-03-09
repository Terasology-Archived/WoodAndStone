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
package org.terasology.crafting.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.workstation.event.WorkstationProcessRequest;

import java.util.List;

public class CraftingWorkstationProcessRequest extends WorkstationProcessRequest {
    private List<String> parameters;
    private int count;

    public CraftingWorkstationProcessRequest() {
    }

    public CraftingWorkstationProcessRequest(EntityRef workstation, String processId, List<String> parameters, int count) {
        super(workstation, processId);
        this.parameters = parameters;
        this.count = count;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public int getCount() {
        return count;
    }
}
