/*
 * Copyright 2016 MovingBlocks
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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.fluid.component.FluidComponent;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.system.FluidRegistry;
import org.terasology.fluid.system.FluidRenderer;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.*;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

public class FluidHolderWidget extends CoreWidget {
    @LayoutConfig
    private Binding<TextureRegion> image = new DefaultBinding<>();
    private InteractionListener listener = new BaseInteractionListener();

    private int minX;
    private int maxX;

    private int minY;
    private int maxY;

    private EntityRef entity;
    private int slotNo;

    public FluidHolderWidget() {
        int x = 0;
    }

    public FluidHolderWidget(String id) {
        super(id);
    }

    public FluidHolderWidget(TextureRegion image) {
        this.image.set(image);
    }

    public FluidHolderWidget(String id, TextureRegion image) {
        super(id);
        this.image.set(image);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (image.get() != null) {
            return image.get().size();
        }
        return Vector2i.zero();
    }

    @Override
    public void onDraw(Canvas canvas) {
        TextureRegion texture = getImage();

        if (texture == null)
        {
            texture = canvas.getCurrentStyle().getBackground();
        }

        if (texture != null) {
            FluidInventoryComponent fluidInventory = entity.getComponent(FluidInventoryComponent.class);
            FluidComponent fluid = fluidInventory.fluidSlots.get(slotNo).getComponent(FluidComponent.class);
            float maxVolume = fluidInventory.maximumVolumes.get(slotNo);

            if (fluid != null) {
                float result = fluid.volume / maxVolume;

                FluidRegistry fluidRegistry = CoreRegistry.get(FluidRegistry.class);
                FluidRenderer fluidRenderer = fluidRegistry.getFluidRenderer(fluid.fluidType);

                Vector2i size = canvas.size();
                if (minY < maxY) {
                    float yPerc = 1f * (minY + result * (maxY - minY)) / texture.getHeight();
                    fluidRenderer.renderFluid(canvas, Rect2i.createFromMinAndSize(minX, minY, maxX, Math.round(yPerc * size.y) - minY));
                } else {
                    float yPerc = 1f * (minY - result * (minY - maxY)) / texture.getHeight();
                    int y = Math.round(yPerc * size.y);
                    fluidRenderer.renderFluid(canvas, Rect2i.createFromMinAndSize(minX, y, maxX, minY - y + 1));
                }
            }

            canvas.drawTexture(texture, canvas.getRegion());
        }

        canvas.addInteractionRegion(listener);
    }

    public void setEntity(EntityRef entity) {
        this.entity = entity;
    }

    public void setSlotNo(int slotNo) {
        this.slotNo = slotNo;
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

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }
}
