// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.durability;

import org.joml.Rectanglei;
import org.joml.Vector2i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureUtil;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.inventory.rendering.nui.layers.ingame.GetItemTooltip;
import org.terasology.inventory.rendering.nui.layers.ingame.InventoryCellRendered;
import org.terasology.nui.Canvas;
import org.terasology.nui.Color;
import org.terasology.nui.util.RectUtility;
import org.terasology.nui.widgets.TooltipLine;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class DurabilityClientSystem extends BaseComponentSystem {
    @ReceiveEvent
    public void drawDurabilityBar(InventoryCellRendered event, EntityRef entity, DurabilityComponent durability) {
        Canvas canvas = event.getCanvas();

        Vector2i size = canvas.size();

        int minX = (int) (size.x * 0.1f);
        int maxX = (int) (size.x * 0.9f);

        int minY = (int) (size.y * 0.8f);
        int maxY = (int) (size.y * 0.9f);

        float durabilityPercentage = 1f * durability.durability / durability.maxDurability;

        if (durabilityPercentage != 1f) {
            ResourceUrn backgroundTexture = TextureUtil.getTextureUriForColor(Color.WHITE);

            final Color terasologyColor = getTerasologyColorForDurability(durabilityPercentage);

            ResourceUrn barTexture = TextureUtil.getTextureUriForColor(terasologyColor);

            canvas.drawTexture(Assets.get(backgroundTexture, Texture.class).get(), new Rectanglei(minX, minY, maxX,
                    maxY));
            int durabilityBarLength = (int) (durabilityPercentage * (maxX - minX - 1));
            int durabilityBarHeight = maxY - minY - 1;
            canvas.drawTexture(Assets.get(barTexture, Texture.class).get(), RectUtility.createFromMinAndSize(minX + 1
                    , minY + 1, durabilityBarLength, durabilityBarHeight));
        }
    }

    private Color getTerasologyColorForDurability(float durabilityPercentage) {
        final java.awt.Color awtColor = java.awt.Color.getHSBColor(0.33f * durabilityPercentage, 1f, 0.8f);

        return new Color(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
    }

    @ReceiveEvent
    public void getDurabilityItemTooltip(GetItemTooltip event, EntityRef entity, DurabilityComponent durability) {
        float durabilityPercentage = 1f * durability.durability / durability.maxDurability;

        final Color color = getTerasologyColorForDurability(durabilityPercentage);
        event.getTooltipLines().add(new TooltipLine("Durability: " + durability.durability + "/" + durability.maxDurability, color));
    }
}
