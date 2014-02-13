package org.terasology.machines.system;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.machines.component.MachineComponent;
import org.terasology.machines.event.MachineStateChanged;
import org.terasology.registry.In;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;
import org.terasology.workstation.system.WorkstationUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class MachineProcessingSystem extends BaseComponentSystem {
    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private Time time;

    @ReceiveEvent
    public void machineAdded(OnAddedComponent event, EntityRef entity, MachineComponent machine, WorkstationComponent workstation) {
        checkForProcessing(entity, workstation);
    }

    @ReceiveEvent
    public void machineStateChanged(MachineStateChanged event, EntityRef entity, MachineComponent machine, WorkstationComponent workstation) {
        checkForProcessing(entity, workstation);
    }

    private void checkForProcessing(EntityRef entity, WorkstationComponent workstation) {
        Set<String> possibleProcesses = new HashSet<>(workstation.supportedProcessTypes);

        WorkstationProcessingComponent processing = entity.getComponent(WorkstationProcessingComponent.class);
        if (processing != null) {
            possibleProcesses.removeAll(processing.processes.keySet());
        }

        for (WorkstationProcess workstationProcess : workstationRegistry.getWorkstationProcesses(possibleProcesses)) {
            try {
                Set<String> possibleResultIds = new HashSet<>();
                for (ProcessPart processPart : workstationProcess.getProcessParts()) {
                    Set<String> resultIds = processPart.validate(entity, entity);
                    if (resultIds != null) {
                        possibleResultIds.addAll(resultIds);
                    }
                }

                if (possibleResultIds.size() <= 1) {
                    String resultId = possibleResultIds.size() == 0 ? null : possibleResultIds.iterator().next();
                    WorkstationUtils.startProcessing(entity, entity, workstationProcess, workstationProcess.getId(), resultId, time.getGameTimeInMs());
                }
            } catch (InvalidProcessException exp) {
                // Ignored - proceed to next process
            }
        }
    }
}
