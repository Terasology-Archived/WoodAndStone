// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.event;

import org.lwjgl.input.Keyboard;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterBindButton(id = "craftInHand", description = "Craft in hand", category = "interaction")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KEY_G)
public class CraftInHandButton extends BindButtonEvent {
}
