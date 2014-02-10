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

import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.gui.events.UIItemIconRendered;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryCellRendered;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

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
    public void drawDurabilityBar(InventoryCellRendered event, EntityRef entity) {
        Canvas canvas = event.getCanvas();

        Vector2i size = canvas.size();

        int minX = (int) (size.x * 0.1f);
        int maxX = (int) (size.x * 0.9f);

        int minY = (int) (size.y * 0.8f);
        int maxY = (int) (size.y * 0.9f);

        DurabilityComponent durability = entity.getComponent(DurabilityComponent.class);
        float durabilityPercentage = durability.durability / durability.maxDurability;

        if (durabilityPercentage != 1f) {
            AssetUri backgroundTexture = TextureUtil.getTextureUriForColor(Color.WHITE);
            AssetUri barTexture = TextureUtil.getTextureUriForColor(new Color(1 - durabilityPercentage, durabilityPercentage, 0));

            canvas.drawTexture(Assets.get(backgroundTexture, Texture.class), Rect2i.createFromMinAndMax(minX, minY, maxX, maxY));
            int durabilityBarLength = (int) (durabilityPercentage * (maxX - minX - 2));
            int durabilityBarHeight = maxY - minY - 2;
            canvas.drawTexture(Assets.get(barTexture, Texture.class), Rect2i.createFromMinAndSize(minX + 1, minY + 1, durabilityBarLength, durabilityBarHeight));
        }
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
