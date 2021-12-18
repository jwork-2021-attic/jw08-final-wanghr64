package java2021.world;

import java.util.*;

public class BulletEnemyAI extends EnemyAI {
    public BulletEnemyAI(Creature creature, World world, Player player) {
        super(creature, world, player);
        creature.setGlyph((char) 130);
    }

    @Override
    public void run() {
        while (creature.hp() > 0) {

            dstX = player.x();
            dstY = player.y();

            int dist = Integer.min(Math.abs(dstX - creature.x()), Math.abs(dstY - creature.y())) + 1;

            bfsRouter();

            if (player.isVisible && rd.nextInt(dist) == 0) {
                int dx = dstX - creature.x();
                int dy = dstY - creature.y();
                Bullet newBullet = null;
                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx < 0)
                        newBullet = new Bullet(world, 2, creature.x() + 1, creature.y());
                    else
                        newBullet = new Bullet(world, 0, creature.x() - 1, creature.y());
                } else {
                    if (dy < 0)
                        newBullet = new Bullet(world, 3, creature.x(), creature.y() - 1);
                    else
                        newBullet = new Bullet(world, 1, creature.x(), creature.y() + 1);
                }
                world.addBullet(newBullet);
                new Thread(newBullet).start();
            }

            if (creature.canSee(dstX, dstY) && player.isVisible && rd.nextInt(5) <= 2) {
                Point nxtPoint = route.peekFirst();
                if (nxtPoint == null)
                    continue;
                if (nxtPoint.x - creature.x() < 0)
                    creature.setGlyph((char) 131);
                else
                    creature.setGlyph((char) 130);
                if (nxtPoint != null) {
                    boolean moved = creature.moveBy(nxtPoint.x - creature.x(), nxtPoint.y - creature.y());
                    if (moved)
                        route.pollFirst();
                }
            } else {
                int r = rd.nextInt(4);
                creature.moveBy(dx[r], dy[r]);
            }
            try {
                Thread.sleep(rd.nextInt(300) + 300);
            } catch (Exception e) {
            }
        }
    }
}
