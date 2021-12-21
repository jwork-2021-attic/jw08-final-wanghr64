package java2021.world;

import java.awt.event.KeyEvent;
import java.util.*;

public class FireBrotherAI extends PlayerAI {

    public FireBrotherAI(Creature creature, World world, List<String> messages) {
        super(creature, world, messages);
    }

    @Override
    public void run() {
        System.out.println("burst fire");
        onSkill = true;
        freeze = true;
        Creature enemy = null;
        int x = player.x();
        int y = player.y();
        System.out.println(player.preDirect);
        for (int i = 1; i < 6; ++i) {
            switch (player.preDirect) {
                case KeyEvent.VK_RIGHT:
                    enemy = world.creature(x + i, y);
                    break;
                case KeyEvent.VK_LEFT:
                    enemy = world.creature(x - i, y);
                    break;
                case KeyEvent.VK_UP:
                    enemy = world.creature(x, y - i);
                    break;
                case KeyEvent.VK_DOWN:
                    enemy = world.creature(x, y + i);
                    break;
            }
            player.attack(enemy);
        }
        try {
            Thread.sleep(800);
        } catch (Exception e) {
        }
        freeze = false;
        onSkill = false;
    }
}