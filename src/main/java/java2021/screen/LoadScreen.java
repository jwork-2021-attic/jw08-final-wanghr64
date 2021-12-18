package java2021.screen;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;

import java2021.world.Bullet;
import java2021.world.Creature;

public class LoadScreen extends SaveLoadScreen {

    public LoadScreen(Screen preScreen) {
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
                if (save[curSelected].exists()) {
                    try {
                        FileInputStream f = new FileInputStream(save[curSelected]);
                        ObjectInputStream o = new ObjectInputStream(f);
                        PlayScreen ps = (PlayScreen) o.readObject();
                        o.close();
                        f.close();
                        for (Bullet b : ps.world().getBullets())
                            b.start();
                        for (Creature c : ps.world().getCreatures())
                            c.startAI();
                        return ps;
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                break;
            default:
                return this;
        }
        return this;
    }
}