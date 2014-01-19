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
package org.terasology.workstation.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.ServerEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ServerEvent
public class UserUpgradeStationRequest implements Event {
    private String stationType;
    private String recipeId;
    private EntityRef instigator;

    public UserUpgradeStationRequest() {
    }

    public UserUpgradeStationRequest(EntityRef instigator, String stationType, String recipeId) {
        this.instigator = instigator;
        this.stationType = stationType;
        this.recipeId = recipeId;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public String getStationType() {
        return stationType;
    }

    public String getRecipeId() {
        return recipeId;
    }
}
