package org.terasology.journal;

import org.lwjgl.input.Keyboard;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.RegisterBindButton;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterBindButton(id = "openJournal", description = "Open journal")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KEY_J)
public class JournalButton extends BindButtonEvent {
}
