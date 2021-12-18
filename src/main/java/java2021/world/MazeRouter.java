package java2021.world;

import java.util.List;
import java.util.Stack;

public class MazeRouter {

    public MazeRouter(World world, int srcX, int srcY, int dstX, int dstY) {
        this.srcX = srcX;
        this.srcY = srcY;
        this.dstX = world.width() - 1;
        this.dstY = world.height() - 2;
        this.world = world;
        route = new Stack<int[]>();

        visited = new boolean[world.width()][world.height()];
        dfsRouter(srcX, srcY);
    }

    private void dfsRouter(int x, int y) {
        if (visited[x][y])
            return;
        visited[x][y] = true;
        route.push(new int[] { x, y });
        if (x == dstX && y == dstY) {
            finished = true;
            return;
        }
        int[][] ds = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : ds) {
            int xx = x + d[0];
            int yy = y + d[1];
            if (!finished && xx >= 0 && xx < world.width() && yy >= 0 && yy < world.height()
                    && world.tile(xx, yy) == Tile.FLOOR)
                dfsRouter(xx, yy);
        }
        if (!finished)
            route.pop();
    }

    public List<int[]> getRoute() {
        return route;
    }

    int srcX;
    int srcY;
    int dstX;
    int dstY;
    World world;

    Stack<int[]> route;
    boolean[][] visited;
    boolean finished;
}
