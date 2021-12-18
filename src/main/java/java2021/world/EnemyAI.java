package java2021.world;

import java.io.Serializable;
import java.util.*;

public abstract class EnemyAI extends CreatureAI {

    protected Random rd;
    protected Player player;
    protected boolean[][] visited;
    protected Point[][] prePoint;
    protected Deque<Point> route;
    protected static int[] dx = new int[] { 1, 0, -1, 0 };
    protected static int[] dy = new int[] { 0, 1, 0, -1 };
    protected int dstX;
    protected int dstY;

    public EnemyAI(Creature creature, World world, Player player) {
        super(creature, world);
        this.player = player;
        this.world = world;
        dstX = player.x();
        dstY = player.y();
        route = new LinkedList<>();
        visited = new boolean[world.width()][world.height()];
        prePoint = new Point[world.width()][world.height()];
        rd = new Random();
        bfsRouter();
    }

    @Override
    public void onEnter(int x, int y, Tile tile) {
        if (tile.isGround()) {
            creature.setX(x);
            creature.setY(y);
        }
    }

    

    protected void bfsRouter() {
        for (int i = 0; i < world.width(); ++i)
            for (int j = 0; j < world.height(); ++j) {
                visited[i][j] = false;
                prePoint[i][j] = null;
            }
        route.clear();
        int srcX = creature.x();
        int srcY = creature.y();
        Deque<Point> q = new LinkedList<>();
        visited[srcX][srcY] = true;
        q.offerLast(new Point(srcX, srcY));
        while (!q.isEmpty()) {
            Point curPoint = q.pollFirst();
            int curX = curPoint.x;
            int curY = curPoint.y;
            if (curX == dstX && curY == dstY)
                break;
            for (int i = 0; i < 4; ++i) {
                int nxtX = curX + dx[i];
                int nxtY = curY + dy[i];
                if (nxtX < 0 || nxtX >= world.width() || nxtY < 0 || nxtY >= world.height())
                    continue;
                if (!visited[nxtX][nxtY]) {
                    visited[nxtX][nxtY] = true;
                    if (world.tile(nxtX, nxtY) == Tile.FLOOR) {
                        prePoint[nxtX][nxtY] = curPoint;
                        q.offerLast(new Point(nxtX, nxtY));
                    }
                }
            }
        }
        for (Point p = prePoint[dstX][dstY]; p != null; p = prePoint[p.x][p.y]) {
            route.addFirst(p);
        }
        route.pollFirst();
    }

    protected class Point implements Serializable {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;

        }

        @Override
        public int hashCode() {
            return x * 100 + y;
        }

        @Override
        public boolean equals(Object p) {
            return ((Point) p).x == this.x && ((Point) p).y == this.y;
        }
    }
}
