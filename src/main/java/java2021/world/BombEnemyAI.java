package java2021.world;

import java.util.*;

public class BombEnemyAI extends EnemyAI {
    public BombEnemyAI(Creature creature, World world, Player player) {
        super(creature, world, player);
        creature.setGlyph((char) 128);
    }

    @Override
    public void run() {
        while (creature.hp() > 0) {
            if (dstX != player.x() || dstY != player.y()) {
                dstX = player.x();
                dstY = player.y();
                bfsRouter();
            }

            if (player.isVisible && Math.abs(player.x() - creature.x()) < 2
                    && Math.abs(player.y() - creature.y()) < 2) {
                creature.attack(player);
                world.remove(creature);
                break;
            }

            if (canSee(dstX, dstY) && player.isVisible) {

                Point nxtPoint = route.peekFirst();
                if (nxtPoint != null) {
                    if (nxtPoint.x - creature.x() < 0)
                        creature.setGlyph((char) 129);
                    else
                        creature.setGlyph((char) 128);
                    boolean moved = creature.moveBy(nxtPoint.x - creature.x(), nxtPoint.y - creature.y());
                    if (moved)
                        route.pollFirst();
                }
            } else {
                int r = rd.nextInt(4);
                creature.moveBy(dx[r], dy[r]);
            }

            try {
                Thread.sleep(rd.nextInt(200) + 100);
            } catch (Exception e) {
            System.out.println(e);
        }
        }
    }
}
