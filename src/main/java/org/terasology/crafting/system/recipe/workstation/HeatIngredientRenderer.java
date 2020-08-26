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
package org.terasology.crafting.system.recipe.workstation;

import org.joml.Rectanglei;
import org.joml.Vector2i;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.nui.Canvas;
import org.terasology.nui.TextLineBuilder;
import org.terasology.nui.asset.font.Font;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class HeatIngredientRenderer implements CraftIngredientRenderer {
    private int requiredTemperature;

    public HeatIngredientRenderer(int requiredTemperature) {
        this.requiredTemperature = requiredTemperature;
    }

    @Override
    public Vector2i getPreferredSize(Canvas canvas, int multiplier) {
        Font font = canvas.getCurrentStyle().getFont();

        List<String> lines = TextLineBuilder.getLines(font, getText(), canvas.size().x);
        Vector2i result = font.getSize(lines);
        return new Vector2i(result.x, result.y + 3);
    }

    @Override
    public void render(Canvas canvas, Rectanglei region, int multiplier) {
        canvas.drawText(getText(), region);
    }

    private String getText() {
        return requiredTemperature + " C";
    }
}
