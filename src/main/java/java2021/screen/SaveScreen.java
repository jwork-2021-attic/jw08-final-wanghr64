package java2021.screen;

import java.awt.event.KeyEvent;
import java.io.*;

public class SaveScreen extends SaveLoadScreen {

    public SaveScreen(Screen preScreen) {
        super(preScreen);
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                return this.preScreen;
            case KeyEvent.VK_W:
                curSelected = (curSelected - 1 + 3) % 3;
                break;
            case KeyEvent.VK_S:
                curSelected = (curSelected + 1) % 3;
                break;
            case KeyEvent.VK_ENTER:
                try {
                    FileOutputStream f = new FileOutputStream(save[curSelected]);
                    ObjectOutputStream o = new ObjectOutputStream(f);
                    o.writeObject(preScreen);
                    o.close();
                    f.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
                break;
            default:
                return this;
        }
        return this;
    }
}
