// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rpgAttributes.system;

import org.terasology.attribute.AttributeManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.GetMaxSpeedEvent;
import org.terasology.engine.registry.In;
import org.terasology.health.logic.event.BeforeDamagedEvent;
import org.terasology.rpgAttributes.RPG;
import org.terasology.rpgAttributes.component.AgilityComponent;
import org.terasology.rpgAttributes.component.ComponentAttributeBaseSource;
import org.terasology.rpgAttributes.component.StrengthComponent;

@RegisterSystem
public class RpgAttributesSystem extends BaseComponentSystem {
    @In
    private AttributeManager attributeManager;

    @Override
    public void preBegin() {
        attributeManager.registerAttribute(RPG.STRENGTH, new ComponentAttributeBaseSource(StrengthComponent.class,
                100));
        attributeManager.registerAttribute(RPG.AGILITY, new ComponentAttributeBaseSource(AgilityComponent.class, 100));
    }

    @ReceiveEvent
    public void doingDamage(BeforeDamagedEvent event, EntityRef damageTarget) {
        final float strength = attributeManager.getAttribute(event.getInstigator(), RPG.STRENGTH);
        if (strength != 100) {
            event.multiply(strength / 100f);
        }
    }

    @ReceiveEvent
    public void impactOnSpeed(GetMaxSpeedEvent event, EntityRef entity) {
        final float agility = attributeManager.getAttribute(entity, RPG.AGILITY);
        if (agility != 100) {
            event.multiply(agility / 100f);
        }
    }
}
