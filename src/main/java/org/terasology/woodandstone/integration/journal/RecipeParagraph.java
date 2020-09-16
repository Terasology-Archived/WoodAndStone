// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.integration.journal;

import org.joml.Vector2i;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.engine.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.Block;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemIcon;
import org.terasology.nui.Canvas;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.util.RectUtility;

public class RecipeParagraph implements ParagraphData, ParagraphRenderable {
    private final int indentAbove = 5;
    private final int indentBelow = 5;
    private final int ingredientSpacing = 3;
    private final int resultSpacing = 30;

    private final int iconSize = 64;
    private final ItemIcon[] ingredientIcons;
    private final ItemIcon resultIcon;

    public RecipeParagraph(Block[] blockIngredients, Prefab[] itemIngredients, Block blockResult, Prefab itemResult,
                           int resultCount) {
        if (blockIngredients.length != itemIngredients.length) {
            throw new IllegalArgumentException("Arrays have to be of the same length");
        }
        ingredientIcons = new ItemIcon[blockIngredients.length];
        for (int i = 0; i < ingredientIcons.length; i++) {
            ItemIcon itemIcon = new ItemIcon();
            if (blockIngredients[i] != null) {
                initializeForBlock(itemIcon, blockIngredients[i]);
            } else {
                initializeForItem(itemIcon, itemIngredients[i]);
            }
            ingredientIcons[i] = itemIcon;
        }
        resultIcon = new ItemIcon();
        if (blockResult != null) {
            initializeForBlock(resultIcon, blockResult);
        } else {
            initializeForItem(resultIcon, itemResult);
        }
        resultIcon.setQuantity(resultCount);
    }

    @Override
    public ParagraphRenderStyle getParagraphRenderStyle() {
        return new ParagraphRenderStyle() {
            @Override
            public HorizontalAlign getHorizontalAlignment() {
                return HorizontalAlign.CENTER;
            }
        };
    }

    @Override
    public ParagraphRenderable getParagraphContents() {
        return this;
    }

    private void initializeForItem(ItemIcon itemIcon, Prefab itemIngredient) {
        ItemComponent item = itemIngredient.getComponent(ItemComponent.class);
        DisplayNameComponent displayName = itemIngredient.getComponent(DisplayNameComponent.class);
        itemIcon.setIcon(item.icon);
        if (displayName != null) {
            itemIcon.setTooltip(displayName.name);
        }
    }

    private void initializeForBlock(ItemIcon itemIcon, Block blockIngredient) {
        itemIcon.setMesh(blockIngredient.getMesh());
        itemIcon.setMeshTexture(Assets.getTexture("engine:terrain").get());
        itemIcon.setTooltip(blockIngredient.getDisplayName());
    }

    @Override
    public void renderContents(Canvas canvas, Vector2i startPos, ContainerRenderSpace containerRenderSpace,
                               int leftIndent, int rightIndent, ParagraphRenderStyle defaultStyle,
                               HorizontalAlign horizontalAlign, HyperlinkRegister hyperlinkRegister) {
        int ingredientsCount = ingredientIcons.length;
        int drawingWidth =
                ingredientsCount * iconSize + (ingredientsCount - 1) * ingredientSpacing + resultSpacing + iconSize;
        int x = startPos.x + horizontalAlign.getOffset(drawingWidth,
                containerRenderSpace.getWidthForVerticalPosition(startPos.y));
        int y = startPos.y + indentAbove;
        for (int i = 0; i < ingredientIcons.length; i++) {
            canvas.drawWidget(ingredientIcons[i], RectUtility.createFromMinAndSize(x, y, iconSize, iconSize));
            x += iconSize + ingredientSpacing;
        }
        x -= ingredientSpacing;
        x += resultSpacing;
        canvas.drawWidget(resultIcon, RectUtility.createFromMinAndSize(x, y, iconSize, iconSize));
    }

    @Override
    public int getPreferredContentsHeight(ParagraphRenderStyle defaultStyle, int yStart,
                                          ContainerRenderSpace containerRenderSpace, int sideIndents) {
        return getPreferredSize().y;
    }

    @Override
    public int getContentsMinWidth(ParagraphRenderStyle defaultStyle) {
        return getPreferredSize().x;
    }

    private Vector2i getPreferredSize() {
        int x = 0;
        int y = 0;

        int ingredientsCount = ingredientIcons.length;
        x += ingredientsCount * iconSize + (ingredientsCount - 1) * ingredientSpacing;
        x += resultSpacing + iconSize;

        y += iconSize;

        y += indentAbove + indentBelow;
        return new Vector2i(x, y);
    }
}
