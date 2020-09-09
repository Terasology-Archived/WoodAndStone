// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.attribute;

import org.terasology.engine.entitySystem.entity.EntityRef;

public interface AttributeBaseSource {
    float getAttributeBase(EntityRef entity);
}
