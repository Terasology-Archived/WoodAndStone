// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.ui;

import org.joml.Vector2i;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.math.TeraMath;
import org.terasology.nui.Canvas;
import org.terasology.nui.Color;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.LayoutConfig;
import org.terasology.nui.ScaleMode;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.util.RectUtility;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class VerticalTextureProgressWidget extends CoreWidget {
    @LayoutConfig
    private Binding<TextureRegion> image = new DefaultBinding<>();
    private int maxY;
    private int minY;
    private Binding<Float> value = new DefaultBinding<>();
    private Binding<Float> mark = new DefaultBinding<>();

    public VerticalTextureProgressWidget() {
        int test = 0;
    }

    public VerticalTextureProgressWidget(String id) {
        super(id);
    }

    public VerticalTextureProgressWidget(TextureRegion image) {
        this.image.set(image);
    }

    public VerticalTextureProgressWidget(String id, TextureRegion image) {
        super(id);
        this.image.set(image);
    }

    @Override
    public void onDraw(Canvas canvas) {
        TextureRegion texture = getImage();
        if (texture != null) {
            float result = TeraMath.clamp(getValue());

            Vector2i size = canvas.size();
            if (minY < maxY) {
                float yPerc = 1f * (minY + result * (maxY - minY)) / texture.getHeight();
                canvas.drawTextureRaw(texture, RectUtility.createFromMinAndSize(0, 0, size.x,
                        Math.round(yPerc * size.y)), ScaleMode.STRETCH,
                        0f, 0f, 1f, yPerc);
            } else {
                float yPerc = 1f * (minY - result * (minY - maxY)) / texture.getHeight();
                canvas.drawTextureRaw(texture, RectUtility.createFromMinAndSize(0, Math.round(yPerc * size.y), size.x
                        , Math.round((1 - yPerc) * size.y)), ScaleMode.STRETCH,
                        0, yPerc, 1, (1 - yPerc));
            }

            Float markValue = getMark();
            if (markValue != null) {
                float yPerc;
                if (minY < maxY) {
                    yPerc = 1f * (minY + markValue * (maxY - minY)) / texture.getHeight();
                } else {
                    yPerc = 1f * (minY - markValue * (minY - maxY)) / texture.getHeight();
                }
                int y = Math.round(yPerc * size.y);
                canvas.drawLine(0, y, size.x, y, Color.BLACK);
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (image.get() != null) {
            return image.get().size();
        }
        return new Vector2i();
    }

    public TextureRegion getImage() {
        return image.get();
    }

    public void setImage(TextureRegion image) {
        this.image.set(image);
    }

    public void bindTexture(Binding<TextureRegion> binding) {
        this.image = binding;
    }

    public float getValue() {
        return value.get();
    }

    public void setValue(float value) {
        this.value.set(value);
    }

    public void bindValue(Binding<Float> binding) {
        this.value = binding;
    }

    public Float getMark() {
        return mark.get();
    }

    public void setMark(Float markValue) {
        mark.set(markValue);
    }

    public void bindMark(Binding<Float> binding) {
        this.mark = binding;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }
}
