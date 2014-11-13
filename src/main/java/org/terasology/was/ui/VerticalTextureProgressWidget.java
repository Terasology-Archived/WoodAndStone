/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.was.ui;

import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

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
            float result = (float) TeraMath.clamp(getValue());

            Vector2i size = canvas.size();
            if (minY < maxY) {
                float yPerc = 1f * (minY + result * (maxY - minY)) / texture.getHeight();
                canvas.drawTextureRaw(texture, Rect2i.createFromMinAndSize(0, 0, size.x, Math.round(yPerc * size.y)), ScaleMode.STRETCH,
                        0f, 0f, 1f, yPerc);
            } else {
                float yPerc = 1f * (minY - result * (minY - maxY)) / texture.getHeight();
                canvas.drawTextureRaw(texture, Rect2i.createFromMinAndSize(0, Math.round(yPerc * size.y), size.x, Math.round((1 - yPerc) * size.y)), ScaleMode.STRETCH,
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
        return Vector2i.zero();
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
