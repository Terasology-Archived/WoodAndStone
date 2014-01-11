package org.terasology.was.system;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.EntityBeingGenerated;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.NoHealthEvent;
import org.terasology.rendering.gui.events.UIItemIconRendered;
import org.terasology.was.component.BlockDropGrammarComponent;
import org.terasology.was.component.DurabilityComponent;
import org.terasology.was.event.DurabilityReducedEvent;
import org.terasology.world.block.BlockComponent;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class DurabilitySystem implements ComponentSystem {
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void addDurabilityToTools(EntityBeingGenerated event, EntityRef entity) {
        String prefabName = event.getPrefabName();
        if (prefabName != null) {
            if (prefabName.equals("Core:axe") || prefabName.equals("Core:pickaxe")) {
                DurabilityComponent durabilityComponent = new DurabilityComponent();
                durabilityComponent.maxDurability = 20;
                durabilityComponent.durability = 20;
                event.addComponent(durabilityComponent);
            }
        }
    }


    @ReceiveEvent(components = {BlockComponent.class})
    public void reduceItemDurability(NoHealthEvent event, EntityRef entity) {
        EntityRef tool = event.getTool();
        DurabilityComponent durabilityComponent = tool.getComponent(DurabilityComponent.class);
        if (durabilityComponent != null) {
            durabilityComponent.durability--;
            tool.saveComponent(durabilityComponent);

            tool.send(new DurabilityReducedEvent());
        }
    }

    @ReceiveEvent(components = {DurabilityComponent.class})
    public void drawDurabilityBarOnItem(UIItemIconRendered event, EntityRef item) {
        DurabilityComponent durability = item.getComponent(DurabilityComponent.class);

        // TODO: Figure out, how to actually draw the durability bar in OpenGL
    }

    @ReceiveEvent(components = {DurabilityComponent.class})
    public void destroyItemOnZeroDurability(DurabilityReducedEvent event, EntityRef entity) {
        DurabilityComponent durability = entity.getComponent(DurabilityComponent.class);
        if (durability.durability == 0) {
            entity.destroy();
        }
    }

    @ReceiveEvent(components = {DurabilityComponent.class})
    public void drawDurabilityBar(UIItemIconRendered event, EntityRef entity) {
        DurabilityComponent durability = entity.getComponent(DurabilityComponent.class);
        int pixels = 42 * durability.durability / durability.maxDurability;

        glColor3f(0, 1, 0);
        glBegin(GL_QUADS);
        glVertex2f(3, 35);
        glVertex2f(pixels, 35);
        glVertex2f(pixels, 38);
        glVertex2f(3, 38);
        glEnd();
    }
}
