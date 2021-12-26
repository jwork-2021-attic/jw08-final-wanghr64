package java2021.world;

import java.util.*;
import java.awt.event.KeyEvent;

public class PowerBrotherAI extends PlayerAI {

    public PowerBrotherAI(Creature creature, World world) {
        super(creature, world);
    }

    @Override
    public void run() {
        onSkill = true;
        freeze = true;
        switch (direction) {
            case KeyEvent.VK_UP:
                player.attack(world.creature(creature.x(), creature.y() - 1));
                break;
            case KeyEvent.VK_DOWN:
                player.attack(world.creature(creature.x(), creature.y() + 1));
                break;
            case KeyEvent.VK_LEFT:
                player.attack(world.creature(creature.x() - 1, creature.y()));
                break;
            case KeyEvent.VK_RIGHT:
                player.attack(world.creature(creature.x() + 1, creature.y()));
                break;
        }
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
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

    private int preX;
    private int preY;
    private int direction;
}
