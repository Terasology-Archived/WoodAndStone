/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.journal;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.input.ButtonState;
import org.terasology.journal.ui.JournalWindow;
import org.terasology.journal.ui.NewEntryWindow;
import org.terasology.logic.manager.GUIManager;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.gui.animation.AnimationOpacity;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.AnimationListener;
import org.terasology.rendering.gui.widgets.UIWindow;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class JournalClientSystem implements UpdateSubscriberSystem {
    @In
    private GUIManager guiManager;
    @In
    private Time time;

    private long timeNotificationOpened = 5000;
    private long lastNotificationReceived;

    private boolean newEntryFull;
    private boolean newEntryFadingOut;

    @Override
    public void initialise() {
        guiManager.registerWindow("Journal:NewEntry", NewEntryWindow.class);
        guiManager.registerWindow("Journal:Journal", JournalWindow.class);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        if (newEntryFull && !newEntryFadingOut && lastNotificationReceived + timeNotificationOpened < time.getGameTimeInMs()) {
            fadeNewEntryWindow();
        }
    }

    private void fadeNewEntryWindow() {
        final UIWindow window = guiManager.getWindowById("Journal:NewEntry");
        if (window != null) {
            AnimationOpacity opacityAnimation = new AnimationOpacity(1f, 0f, 10f);
            window.addAnimation(opacityAnimation);
            opacityAnimation.addAnimationListener(
                    new AnimationListener() {
                        @Override
                        public void start(UIDisplayElement element) {
                        }

                        @Override
                        public void stop(UIDisplayElement element) {
                            window.close();
                            newEntryFadingOut = false;
                        }

                        @Override
                        public void repeat(UIDisplayElement element) {
                        }
                    });
            opacityAnimation.start();
            newEntryFull = false;
            newEntryFadingOut = true;
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void openJournal(JournalButton event, EntityRef character) {
        if (event.getState() == ButtonState.DOWN) {
            guiManager.openWindow("Journal:Journal");
        }
    }

    @ReceiveEvent
    public void newEntryNotificationReceived(NewJournalEntryDiscoveredEvent event, EntityRef character) {
        lastNotificationReceived = time.getGameTimeInMs();
        newEntryFull = true;
        guiManager.openWindow("Journal:NewEntry");
    }
}
