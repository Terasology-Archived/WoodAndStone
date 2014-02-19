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
package org.terasology.was.journal;

import org.terasology.journal.JournalManager;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.HorizontalAlign;

public class RecipeJournalPart implements JournalManager.JournalEntryPart {
    private int indentAbove = 5;
    private int indentBelow = 5;
    private int ingredientSpacing = 3;
    private int resultSpacing = 30;
    private TextureRegion[] ingredients;
    private TextureRegion result;
    private int iconSize = 64;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;

    public RecipeJournalPart(TextureRegion[] ingredients, TextureRegion result) {
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public Vector2i getPreferredSize(Canvas canvas, long date) {
        int x = 0;
        int y = 0;

        int ingredientsCount = ingredients.length;
        x += ingredientsCount * iconSize + (ingredientsCount - 1) * ingredientSpacing;
        x += resultSpacing + iconSize;

        y += iconSize;

        y += indentAbove + indentBelow;
        return new Vector2i(x, y);
    }

    @Override
    public void render(Canvas canvas, Rect2i region, long date) {
        int ingredientsCount = ingredients.length;
        int drawingWidth = ingredientsCount * iconSize + (ingredientsCount - 1) * ingredientSpacing + resultSpacing + iconSize;
        int x = region.minX() + horizontalAlign.getOffset(drawingWidth, region.width());
        int y = region.minY() + indentAbove;
        for (int i = 0; i < ingredients.length; i++) {
            canvas.drawTexture(ingredients[i], Rect2i.createFromMinAndSize(x, y, iconSize, iconSize));
            x += iconSize + ingredientSpacing;
        }
        x -= ingredientSpacing;
        x += resultSpacing;
        canvas.drawTexture(result, Rect2i.createFromMinAndSize(x, y, iconSize, iconSize));
    }
}
