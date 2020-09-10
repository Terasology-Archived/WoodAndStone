// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.npcAI.component;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.nui.reflection.MappedContainer;

import java.util.List;

public class AggroComponent implements Component {
    public List<AggroValue> aggroValues = Lists.newArrayList();
    public EntityRef aggroTarget = EntityRef.NULL;

    @MappedContainer
    public static class AggroValue {
        public long time;
        public int amount;
        public EntityRef instigator;
    }
}
