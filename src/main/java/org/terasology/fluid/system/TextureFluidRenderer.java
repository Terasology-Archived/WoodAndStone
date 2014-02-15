package org.terasology.fluid.system;

import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Canvas;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class TextureFluidRenderer implements FluidRenderer {
    private Texture texture;

    public TextureFluidRenderer(Texture texture) {
        this.texture = texture;
    }

    @Override
    public void renderFluid(Canvas canvas, Rect2i region) {
        Vector2i size = canvas.size();
        int x = Math.round(size.x * region.minX() / 32f);
        int y = Math.round(size.y * region.minY() / 32f);
        int width = Math.round(size.x * region.width() / 32f);
        int height = Math.round(size.y * region.height() / 32f);
        canvas.drawTexture(texture, Rect2i.createFromMinAndSize(x, y, width, height));
    }
}
