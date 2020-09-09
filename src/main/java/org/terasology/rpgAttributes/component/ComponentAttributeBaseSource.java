// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rpgAttributes.component;

import org.terasology.attribute.AttributeBaseSource;
import org.terasology.engine.entitySystem.entity.EntityRef;

public class ComponentAttributeBaseSource implements AttributeBaseSource {
    private final Class<? extends AttributeComponent> attributeComponent;
    private final int defaultValue;

    public ComponentAttributeBaseSource(Class<? extends AttributeComponent> attributeComponent, int defaultValue) {
        this.attributeComponent = attributeComponent;
        this.defaultValue = defaultValue;
    }

    @Override
    public float getAttributeBase(EntityRef entity) {
        final AttributeComponent attribute = entity.getComponent(attributeComponent);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.value;
    }
}
