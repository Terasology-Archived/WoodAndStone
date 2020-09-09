// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.workstation.event.WorkstationProcessRequest;

import java.util.List;

public class CraftingWorkstationProcessRequest extends WorkstationProcessRequest {
    private List<String> parameters;
    private int count;

    public CraftingWorkstationProcessRequest() {
    }

    public CraftingWorkstationProcessRequest(EntityRef workstation, String processId, List<String> parameters,
                                             int count) {
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
