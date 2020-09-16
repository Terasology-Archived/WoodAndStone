// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.milling.component;

import org.terasology.engine.entitySystem.Component;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class MillProcessedComponent implements Component {
    public long millLength;
    public String blockResult;
    public String itemResult;
}
