package java2021.screen;

import java2021.asciiPanel.AsciiPanel;
import java.io.File;
import java.util.Date;

public abstract class SaveLoadScreen implements Screen {

    public SaveLoadScreen(Screen preScreen) {
        this.preScreen = preScreen;
    }

    protected final Screen preScreen;
    protected int curSelected;
    protected File[] save = new File[] { new File("saves/save1.sav"), new File("saves/save2.sav"),
            new File("saves/save3.sav") };

    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.write("Save 1:", 7, 7);
        terminal.write("Save 2:", 7, 9);
        terminal.write("Save 3:", 7, 11);
        terminal.write("!", 6, 7 + curSelected * 2, AsciiPanel.brightBlue);
        for (int i = 0; i < 3; ++i) {
            if (save[i].exists())
                terminal.write(new Date(save[i].lastModified()).toString(), 9, 8 + 2 * i);
            else
                terminal.write("No Save", 9, 8 + 2 * i);
        }

    }
}
