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
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.Share;
import org.terasology.workstation.component.ProcessDefinitionComponent;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = {WorkstationRegistry.class})
public class WorkstationRegistryImpl extends BaseComponentSystem implements WorkstationRegistry {
    private Set<String> scannedTypes = new HashSet<>();

    private Map<String, Map<String, WorkstationProcess>> workstationProcesses = new LinkedHashMap<>();

    @Override
    public void registerProcessFactory(String processType, WorkstationProcessFactory factory) {
        registerProcesses(processType, factory);
    }

    @Override
    public Collection<WorkstationProcess> getWorkstationProcesses(Collection<String> processTypes) {
        List<WorkstationProcess> processes = new LinkedList<>();
        for (String processType : processTypes) {
            if (!scannedTypes.contains(processType)) {
                registerProcesses(processType, new DefaultWorkstationProcessFactory());
            }
            processes.addAll(workstationProcesses.get(processType).values());
        }

        return processes;
    }

    @Override
    public void registerProcess(String processType, WorkstationProcess workstationProcess) {
        Map<String, WorkstationProcess> processes = workstationProcesses.get(processType);
        if (processes == null) {
            processes = new HashMap<>();
            workstationProcesses.put(processType, processes);
        }
        processes.put(workstationProcess.getId(), workstationProcess);
    }

    @Override
    public WorkstationProcess getWorkstationProcessById(Collection<String> supportedProcessTypes, String processId) {
        for (WorkstationProcess workstationProcess : getWorkstationProcesses(supportedProcessTypes)) {
            if (workstationProcess.getId().equals(processId)) {
                return workstationProcess;
            }
        }
        return null;
    }

    private void registerProcesses(String processType, WorkstationProcessFactory factory) {
        Map<String, WorkstationProcess> processes = new HashMap<>();
        if (workstationProcesses.containsKey(processType)) {
            processes.putAll(workstationProcesses.get(processType));
        }
        PrefabManager prefabManager = CoreRegistry.get(PrefabManager.class);

        for (Prefab prefab : prefabManager.listPrefabs(ProcessDefinitionComponent.class)) {
            ProcessDefinitionComponent processDef = prefab.getComponent(ProcessDefinitionComponent.class);
            if (processDef.processType.equals(processType)) {
                WorkstationProcess process = factory.createProcess(prefab);
                processes.put(process.getId(), process);
            }
        }
        workstationProcesses.put(processType, processes);
        scannedTypes.add(processType);
    }
}
