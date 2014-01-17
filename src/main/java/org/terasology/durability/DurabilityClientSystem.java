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
package org.terasology.durability;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.rendering.gui.events.UIItemIconRendered;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class DurabilityClientSystem implements ComponentSystem {
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {DurabilityComponent.class})
    public void drawDurabilityBar(UIItemIconRendered event, EntityRef entity) {
        DurabilityComponent durability = entity.getComponent(DurabilityComponent.class);
        int pixels = 36 * durability.durability / durability.maxDurability;

        glColor3f(1, 1, 1);
        glBegin(GL_QUADS);
        glVertex2f(3, 34);
        glVertex2f(41, 34);
        glVertex2f(41, 39);
        glVertex2f(3, 39);
        glEnd();

        glColor3f(0, 1, 0);
        glBegin(GL_QUADS);
        glVertex2f(4, 35);
        glVertex2f(4 + pixels, 35);
        glVertex2f(4 + pixels, 38);
        glVertex2f(4, 38);
        glEnd();
    }
}
