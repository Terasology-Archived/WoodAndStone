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
package org.terasology.was.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.ServerEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ServerEvent
public class UserCraftInHandRequest implements Event {
    private EntityRef item1;
    private EntityRef item2;
    private EntityRef item3;

    public UserCraftInHandRequest() {
    }

    public UserCraftInHandRequest(EntityRef item1, EntityRef item2, EntityRef item3) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
    }

    public EntityRef getItem1() {
        return item1;
    }

    public EntityRef getItem2() {
        return item2;
    }

    public EntityRef getItem3() {
        return item3;
    }
}
