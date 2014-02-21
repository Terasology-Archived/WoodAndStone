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
package org.terasology.heat.ui;

import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class TermometerWidget extends CoreWidget {
    private Binding<Float> temperature = new DefaultBinding<>();
    private Binding<Float> markedTemperature = new DefaultBinding<>();
    private Binding<Float> maxTemperature = new DefaultBinding<>();
    private Binding<Float> minTemperature = new DefaultBinding<>();
    private InteractionListener listener = new BaseInteractionListener();

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return sizeHint;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart("front");
        TextureRegion foreground = canvas.getCurrentStyle().getBackground();
        float min = getMinTemperature(); // 0.87f of whole height
        float max = getMaxTemperature(); // 0.07f of whole height

        float current = getTemperature();

        Vector2i size = canvas.size();

        float temperaturePerc = 0.87f - (current - min) / (max - min) * 0.8f;

        canvas.drawTextureRaw(foreground,
                Rect2i.createFromMinAndSize(0, Math.round(temperaturePerc * size.y), size.x,
                        Math.round((1 - temperaturePerc) * size.y)),
                ScaleMode.STRETCH, 0, temperaturePerc, 1, (1 - temperaturePerc));

        Float markValue = getMarkedTemperature();
        if (markValue != null) {
            float markPerc = 0.87f - (markValue - min) / (max - min) * 0.8f;
            int y = Math.round(markPerc * size.y);
            canvas.drawLine(0, y, size.x, y, Color.BLACK);
        }

        canvas.addInteractionRegion(listener);
    }

    public void setTemperature(float value) {
        temperature.set(value);
    }

    public void bindTemperature(Binding<Float> temperature) {
        this.temperature = temperature;
    }

    public float getTemperature() {
        return temperature.get();
    }

    public void setMarkedTemperature(Float value) {
        temperature.set(value);
    }

    public void bindMarkedTemperature(Binding<Float> markedTemperature) {
        this.markedTemperature = markedTemperature;
    }

    public Float getMarkedTemperature() {
        return markedTemperature.get();
    }

    public void setMaxTemperature(float value) {
        maxTemperature.set(value);
    }

    public void bindMaxTemperature(Binding<Float> maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public float getMaxTemperature() {
        return maxTemperature.get();
    }

    public void setMinTemperature(float value) {
        minTemperature.set(value);
    }

    public void bindMinTemperature(Binding<Float> minTemperature) {
        this.minTemperature = minTemperature;
    }

    public float getMinTemperature() {
        return minTemperature.get();
    }
}
