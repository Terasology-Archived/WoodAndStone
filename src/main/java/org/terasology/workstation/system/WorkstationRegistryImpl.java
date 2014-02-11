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
package org.terasology.workstation.system;

import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.workstation.component.ProcessDefinitionComponent;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = {WorkstationRegistry.class})
public class WorkstationRegistryImpl implements ComponentSystem, WorkstationRegistry {
    private Map<String, WorkstationProcess> workstationProcesses = new LinkedHashMap<>();

    @In
    private PrefabManager prefabManager;

    @Override
    public void initialise() {
        registerPrefabRecipes();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void registerProcess(WorkstationProcess workstationProcess) {
        workstationProcesses.put(workstationProcess.getId(), workstationProcess);
    }

    @Override
    public Collection<WorkstationProcess> getWorkstationProcesses() {
        return workstationProcesses.values();
    }

    @Override
    public WorkstationProcess getWorkstationProcessById(String processId) {
        return workstationProcesses.get(processId);
    }

    private void registerPrefabRecipes() {
        for (Prefab prefab : prefabManager.listPrefabs(ProcessDefinitionComponent.class)) {
            registerProcess(new WorkstationProcessFromPrefab(prefab));
        }
    }
}
