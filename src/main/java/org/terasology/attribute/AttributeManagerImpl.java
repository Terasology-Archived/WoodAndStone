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
package org.terasology.attribute;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.Share;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem
@Share(AttributeManager.class)
public class AttributeManagerImpl extends BaseComponentSystem implements AttributeManager {
    private Map<String, AttributeBaseSource> attributeBases = new HashMap<>();

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
