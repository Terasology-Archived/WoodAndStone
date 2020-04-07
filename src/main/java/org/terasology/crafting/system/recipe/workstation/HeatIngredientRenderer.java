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

import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.TextLineBuilder;

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
        Vector2i result = JomlUtil.from(font.getSize(lines));
        return new Vector2i(result.x, result.y + 3);
    }

    @Override
    public void render(Canvas canvas, Rect2i region, int multiplier) {
        canvas.drawText(getText(), region);
    }

    private String getText() {
        return requiredTemperature + " C";
    }
}
