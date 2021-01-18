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
package org.terasology.crafting.ui.workstation;

import org.terasology.joml.geom.Rectanglei;
import org.joml.Vector2i;
import org.terasology.input.MouseInput;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.InteractionListener;
import org.terasology.nui.TextLineBuilder;
import org.terasology.nui.asset.font.Font;
import org.terasology.nui.events.NUIMouseClickEvent;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class RecipeCategoryWidget extends CoreWidget {
    private boolean opened;
    private int leftIndent;
    private String name;
    private int count;
    private CategoryToggleCallback callback;

    private InteractionListener interactionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                callback.categoryToggled();
                return true;
            }
            return false;
        }
    };

    public RecipeCategoryWidget(boolean opened, int leftIndent, String name, int count,
                                CategoryToggleCallback callback) {
        this.opened = opened;
        this.leftIndent = leftIndent;
        this.name = name;
        this.count = count;
        this.callback = callback;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Vector2i size = canvas.size();
        canvas.drawText(getText(), new Rectanglei(leftIndent, 0, size.x, size.y));
        canvas.addInteractionRegion(interactionListener, "Toggle " + name);
    }

    private String getText() {
        return (opened ? "-" : "+") + " " + String.format("%s (%d)", name, count);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        Font font = canvas.getCurrentStyle().getFont();

        List<String> lines = TextLineBuilder.getLines(font, getText(), canvas.size().x);
        Vector2i textSize = font.getSize(lines);
        return new Vector2i(canvas.size().x, textSize.y);
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return getPreferredContentSize(canvas, null);
    }
}
