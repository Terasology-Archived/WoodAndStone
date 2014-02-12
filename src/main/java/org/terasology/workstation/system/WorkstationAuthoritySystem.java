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

import org.terasology.crafting.component.CraftingStationComponent;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.AddDelayedActionEvent;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.registry.In;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.event.OpenWorkstationRequest;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class WorkstationAuthoritySystem implements ComponentSystem {
    private static final String WORKSTATION_PROCESSING = "Workstation:Processing";

    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private Time time;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CraftingStationComponent.class})
    public void userActivatesWorkstation(ActivateEvent event, EntityRef entity) {
        entity.send(new OpenWorkstationRequest());
    }

    @ReceiveEvent
    public void finishProcessing(DelayedActionTriggeredEvent event, EntityRef workstation, WorkstationComponent workstationComp) {
        if (event.getActionId().equals(WORKSTATION_PROCESSING)) {
            String processId = workstationComp.processingProcessId;
            WorkstationProcess process = workstationRegistry.getWorkstationProcessById(workstationComp.supportedProcessTypes, processId);
            for (ProcessPart processPart : process.getProcessParts()) {
                processPart.executeEnd(workstation, workstation, workstationComp.processingResultId);
            }

            workstationComp.processingProcessId = null;
            workstationComp.processingResultId = null;
            workstation.saveComponent(workstationComp);
        }
    }

    @ReceiveEvent
    public void manualWorkstationProcess(WorkstationProcessRequest event, EntityRef workstation, WorkstationComponent workstationComp) {
        String processId = event.getProcessId();
        String resultId = event.getResultId();

        WorkstationProcess process = workstationRegistry.getWorkstationProcessById(workstationComp.supportedProcessTypes, processId);
        if (process != null) {
            List<ProcessPart> processParts = process.getProcessParts();

            long duration = 0;
            Set<String> validResults = new HashSet<>();
            for (ProcessPart processPart : processParts) {
                try {
                    Set<String> results = processPart.validate(event.getInstigator(), workstation);
                    if (results != null) {
                        validResults.addAll(results);
                    }
                } catch (InvalidProcessException exp) {
                    return;
                }
            }

            if (resultId != null && !validResults.contains(resultId)) {
                return;
            }

            for (ProcessPart processPart : processParts) {
                duration += processPart.getDuration(event.getInstigator(), workstation, resultId);
            }

            for (ProcessPart processPart : processParts) {
                processPart.executeStart(event.getInstigator(), workstation, resultId);
            }

            if (duration > 0) {
                long gameTime = time.getGameTimeInMs();
                workstationComp.processingStartTime = gameTime;
                workstationComp.processingFinishTime = gameTime + duration;
                workstationComp.processingProcessId = processId;
                workstationComp.processingResultId = resultId;
                workstation.saveComponent(workstationComp);

                workstation.send(new AddDelayedActionEvent(WORKSTATION_PROCESSING, duration));
            } else {
                for (ProcessPart processPart : processParts) {
                    processPart.executeEnd(event.getInstigator(), workstation, resultId);
                }
            }
        }
    }
}
