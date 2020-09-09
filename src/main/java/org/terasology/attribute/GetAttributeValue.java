// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.attribute;

import org.terasology.engine.entitySystem.event.AbstractValueModifiableEvent;

public class GetAttributeValue extends AbstractValueModifiableEvent {
    private final String attributeName;

    public GetAttributeValue(String attributeName, float baseValue) {
        super(baseValue);
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
