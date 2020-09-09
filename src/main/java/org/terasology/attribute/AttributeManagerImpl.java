// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.attribute;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.Share;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem
@Share(AttributeManager.class)
public class AttributeManagerImpl extends BaseComponentSystem implements AttributeManager {
    private final Map<String, AttributeBaseSource> attributeBases = new HashMap<>();

    @Override
    public void registerAttribute(String attributeName, AttributeBaseSource attributeBaseSource) {
        attributeBases.put(attributeName, attributeBaseSource);
    }

    @Override
    public float getAttribute(EntityRef entity, String attributeName) {
        final AttributeBaseSource attributeBaseSource = attributeBases.get(attributeName);
        float attributeBaseValue = 0;
        if (attributeBaseSource != null) {
            attributeBaseValue = attributeBaseSource.getAttributeBase(entity);
        }
        GetAttributeValue attributeEvent = new GetAttributeValue(attributeName, attributeBaseValue);
        entity.send(attributeEvent);
        return attributeEvent.getResultValue();
    }
}
