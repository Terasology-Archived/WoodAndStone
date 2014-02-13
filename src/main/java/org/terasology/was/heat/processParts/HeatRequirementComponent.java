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
package org.terasology.was.heat.processParts;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.registry.CoreRegistry;
import org.terasology.was.heat.HeatUtils;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.world.BlockEntityRegistry;

import java.util.Set;

public class HeatRequirementComponent implements Component, ProcessPart {
    public float heat;

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation) throws InvalidProcessException {
        if (HeatUtils.calculateHeatForEntity(workstation, CoreRegistry.get(BlockEntityRegistry.class)) < heat) {
            throw new InvalidProcessException();
        }
        return null;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String result) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String result) {
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result) {
    }
}
