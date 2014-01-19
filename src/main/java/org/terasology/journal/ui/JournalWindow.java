package org.terasology.journal.ui;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.journal.JournalManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Vector2i;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class JournalWindow extends UIWindow {

    private final UIText text;

    public JournalWindow() {
        setId("Journal:Journal");
        setModal(true);

        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE});

        maximize();

        Vector2i displaySize = getDisplaySize();

        int height = displaySize.y - 100;
        int width = 3 * height / 4;

        setHorizontalAlign(EHorizontalAlign.CENTER);
        setVerticalAlign(EVerticalAlign.CENTER);

        UIImage background = new UIImage(Assets.getTexture("WoodAndStone:JournalBackground"));
        background.setSize(new Vector2f(width, height));
        background.setPosition(new Vector2f(0, 0));

        addDisplayElement(background);

        text = new UIText();
        text.setColor(Color.black);
        text.setBackgroundColor(new Color(0, 0, 0, 0));
        text.setDisabled(true);
        text.setMultiLine(true);
        text.setSize(new Vector2f(width - 100, height - 100));
        text.setPosition(new Vector2f(50, 50));

        addDisplayElement(text);

        setSize(new Vector2f(width, height));

        updateJournal();
    }

    public void updateJournal() {
        StringBuilder sb = new StringBuilder();

        JournalManager journalManager = CoreRegistry.get(JournalManager.class);
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        Map<JournalManager.JournalChapter, List<String>> playerEntries = journalManager.getPlayerEntries(playerEntity);
        for (List<String> chapterContents : playerEntries.values()) {
            boolean first = true;
            for (String chapterEntry : chapterContents) {
                if (!first) {
                    sb.append("\n\n");
                } else {
                    first = false;
                }
                sb.append(chapterEntry);
            }
        }

        text.setText(sb.toString());
        text.scrollToBottom();
    }
}
