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

import org.joml.Vector2i;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.InteractionListener;
import org.terasology.nui.ScaleMode;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.Color;
import org.terasology.nui.util.RectUtility;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ThermometerWidget extends CoreWidget {
    private Binding<Float> temperature = new DefaultBinding<>();
    private Binding<Float> markedTemperature = new DefaultBinding<>();
    private Binding<Float> maxTemperature = new DefaultBinding<>();
    private Binding<Float> minTemperature = new DefaultBinding<>();
    private InteractionListener listener = new BaseInteractionListener();

    private float minHeightPerc = 0.87f;
    private float maxHeightPerc = 0.03f;

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return sizeHint;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart("front");
        UITextureRegion foreground = canvas.getCurrentStyle().getBackground();
        float min = getMinTemperature();
        float max = getMaxTemperature();

        float current = getTemperature();

        Vector2i size = canvas.size();

        float temperaturePerc = minHeightPerc - (current - min) / (max - min) * (minHeightPerc - maxHeightPerc);

        canvas.drawTextureRaw(foreground,
                RectUtility.createFromMinAndSize(0, Math.round(temperaturePerc * size.y), size.x,
                        Math.round((1 - temperaturePerc) * size.y)),
                ScaleMode.STRETCH, 0, temperaturePerc, 1, (1 - temperaturePerc));

        Float markValue = getMarkedTemperature();
        if (markValue != null) {
            float markPerc = minHeightPerc - (markValue - min) / (max - min) * (minHeightPerc - maxHeightPerc);
            int y = Math.round(markPerc * size.y);
            canvas.drawLine(0, y, size.x, y, Color.BLACK);
        }

        canvas.addInteractionRegion(listener);
    }

    public void setTemperature(float value) {
        temperature.set(value);
    }

    public void bindTemperature(Binding<Float> temperatureToBind) {
        temperature = temperatureToBind;
    }

    public float getTemperature() {
        return temperature.get();
    }

    public void setMarkedTemperature(Float value) {
        temperature.set(value);
    }

    public void bindMarkedTemperature(Binding<Float> markedTemperatureToBind) {
        markedTemperature = markedTemperatureToBind;
    }

    public Float getMarkedTemperature() {
        return markedTemperature.get();
    }

    public void setMaxTemperature(float value) {
        maxTemperature.set(value);
    }

    public void bindMaxTemperature(Binding<Float> maxTemperatureToBind) {
        maxTemperature = maxTemperatureToBind;
    }

    public float getMaxTemperature() {
        return maxTemperature.get();
    }

    public void setMinTemperature(float value) {
        minTemperature.set(value);
    }

    public void bindMinTemperature(Binding<Float> minTemperatureToBind) {
        minTemperature = minTemperatureToBind;
    }

    public float getMinTemperature() {
        return minTemperature.get();
    }
}
