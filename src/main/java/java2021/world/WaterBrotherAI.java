package java2021.world;

import java.util.*;

public class WaterBrotherAI extends PlayerAI {

    public WaterBrotherAI(Creature creature, World world, List<String> messages) {
        super(creature, world, messages);
    }

    @Override
    public void run() {
        onSkill = true;
        freeze = true;
        int r = 5;
        int x = player.x();
        int y = player.y();
        for (int i = 0; i < r; ++i)
            for (int j = 0; j < r - i; ++j) {
                if (i == 0 && j == 0)
                    continue;
                player.attack(world.creature(x + j, y + i));
                world.removeBullet(world.bullet(x + j, y + i));
                if (i != 0) {
                    player.attack(world.creature(x + j, y - i));
                    world.removeBullet(world.bullet(x + j, y + i));
                }
                if (j != 0) {
                    player.attack(world.creature(x - j, y + i));
                    world.removeBullet(world.bullet(x + j, y + i));
                }
                if (i != 0 && j != 0) {
                    player.attack(world.creature(x - j, y - i));
                    world.removeBullet(world.bullet(x + j, y + i));
                }
            }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        freeze = false;
        onSkill = false;
    }
}