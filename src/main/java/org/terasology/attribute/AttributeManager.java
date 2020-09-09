// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.attribute;

import org.terasology.engine.entitySystem.entity.EntityRef;

public interface AttributeManager {
    void registerAttribute(String attributeName, AttributeBaseSource attributeBaseSource);

    float getAttribute(EntityRef entity, String attributeName);
}
