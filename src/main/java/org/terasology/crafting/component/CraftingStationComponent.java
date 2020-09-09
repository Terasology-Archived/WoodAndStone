// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.rendering.assets.texture.Texture;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationComponent implements Component {
    public String type;
    public Texture workstationUITexture;
}
