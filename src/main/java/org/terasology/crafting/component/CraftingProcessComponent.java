// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.component;

import org.terasology.engine.entitySystem.Component;

import java.util.List;

public class CraftingProcessComponent implements Component {
    public List<String> parameters;
    public int count;
}
