package java2021.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
 * Copyright (C) 2015 s-zhouj
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/**
 *
 * @author s-zhouj
 */
public class WorldBuilder {

    private int width;
    private int height;
    private Tile[][] tiles;

    public WorldBuilder(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        this.visited = new boolean[width][height];
    }

    public World build() {
        return new World(tiles);
    }

    private WorldBuilder randomizeTiles() {
        for (int width = 3; width < this.width - 3; width++) {
            for (int height = 3; height < this.height - 3; height++) {
                Random rand = new Random();
                switch (rand.nextInt(World.TILE_TYPES)) {
                case 0:
                    tiles[width][height] = Tile.FLOOR;
                    break;
                case 1:
                    tiles[width][height] = Tile.WALL;
                    break;
                }
            }
        }
        return this;
    }

    private WorldBuilder smooth(int factor) {
        Tile[][] newtemp = new Tile[width][height];
        if (factor > 1) {
            smooth(factor - 1);
        }
        for (int width = 0; width < this.width; width++) {
            for (int height = 0; height < this.height; height++) {
                // Surrounding walls and floor
                int surrwalls = 0;
                int surrfloor = 0;

                // Check the tiles in a 3x3 area around center tile
                for (int dwidth = -1; dwidth < 2; dwidth++) {
                    for (int dheight = -1; dheight < 2; dheight++) {
                        if (width + dwidth < 0 || width + dwidth >= this.width || height + dheight < 0
                                || height + dheight >= this.height) {
                            continue;
                        } else if (tiles[width + dwidth][height + dheight] == Tile.FLOOR) {
                            surrfloor++;
                        } else if (tiles[width + dwidth][height + dheight] == Tile.WALL) {
                            surrwalls++;
                        }
                    }
                }
                Tile replacement;
                if (surrwalls > surrfloor) {
                    replacement = Tile.WALL;
                } else {
                    replacement = Tile.FLOOR;
                }
                newtemp[width][height] = replacement;
            }
        }
        tiles = newtemp;
        return this;
    }

    private WorldBuilder randomizeMaze() {
        for (int i = 0; i < this.width; ++i)
            for (int j = 0; j < this.height; ++j)
                this.tiles[i][j] = Tile.WALL;
        dfsBuilder(this.width - 1, this.height - 2, 0, 1);
        this.tiles[0][1] = Tile.FLOOR;
        this.tiles[1][1] = Tile.FLOOR;
        return this;
    }

    private static List<int[]> ds = new ArrayList<int[]>(
            Arrays.asList(new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } }));

    private boolean[][] visited;

    private void dfsBuilder(int x, int y, int dstX, int dstY) {
        visited[x][y] = true;
        this.tiles[x][y] = Tile.FLOOR;
        if (x == dstX + 1 && y == dstY)
            return;

        Collections.shuffle(ds);
        for (int[] d : ds) {
            int xx = x + d[0];
            int yy = y + d[1];
            if (xx > 0 && xx < this.width - 1 && yy > 0 && yy < this.height - 1 && !visited[xx][yy] && check(xx, yy)) {
                dfsBuilder(xx, yy, dstX, dstY);
            }
        }
    }

    private boolean check(int x, int y) {
        int temp_sum = 0;
        int[][] ds = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : ds)
            temp_sum += this.tiles[x + d[0]][y + d[1]] == Tile.WALL ? 1 : 0;
        return temp_sum >= 3;
    }

    public WorldBuilder makeCaves() {
        return randomizeTiles().smooth(8);
    }

    public WorldBuilder makeMaze() {
        return this.randomizeMaze();
    }
}
