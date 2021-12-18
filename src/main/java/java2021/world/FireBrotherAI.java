package java2021.world;

import java.awt.event.KeyEvent;
import java.util.*;

public class FireBrotherAI extends PlayerAI {

    int direction;

    public FireBrotherAI(Creature creature, World world, List<String> messages) {
        super(creature, world, messages);
    }

    @Override
    public void run() {
        onSkill = true;
        freeze = true;
        Creature enemy = null;
        int x = player.x();
        int y = player.y();
        for (int i = 1; i < 6; ++i) {
            switch (direction) {
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
            if (enemy != null)
                player.attack(enemy);
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        freeze = false;
        onSkill = false;
    }

    @Override
    public void onEnter(int x, int y, Tile tile) {
        // decide which direction to burst fire.
        int preX = player.x();
        int preY = player.y();
        if (x - preX == 1)
            direction = KeyEvent.VK_RIGHT;
        else if (x - preX == -1)
            direction = KeyEvent.VK_LEFT;
        else if (y - preY == 1)
            direction = KeyEvent.VK_DOWN;
        else if (y - preY == -1)
            direction = KeyEvent.VK_UP;
        if (tile.isGround()) {
            creature.setX(x);
            creature.setY(y);
        } else if (tile.isDiggable() && player.digCount > 0) {
            creature.dig(x, y);
            --player.digCount;
        }
    }
}