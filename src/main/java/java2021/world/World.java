package java2021.world;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/*
 * Copyright (C) 2015 Aeranythe Echosong
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
 * @author Aeranythe Echosong
 */
public class World implements Serializable {

    private Tile[][] tiles;
    private int width;
    private int height;
    private ConcurrentLinkedDeque<Creature> creatures;
    private ConcurrentLinkedDeque<Bullet> bullets;
    private List<Bonus> bonuses;

    public static final int TILE_TYPES = 2;

    public World(Tile[][] tiles) {
        this.tiles = tiles;
        this.width = tiles.length;
        this.height = tiles[0].length;
        this.creatures = new ConcurrentLinkedDeque<>();
        this.bonuses = new LinkedList<>();
        this.bullets = new ConcurrentLinkedDeque<>();
    }

    public Tile tile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Tile.BOUNDS;
        } else {
            return tiles[x][y];
        }
    }

    public char glyph(int x, int y) {
        return tiles[x][y].glyph();
    }

    public Color color(int x, int y) {
        return tiles[x][y].color();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void dig(int x, int y) {
        if (tile(x, y).isDiggable()) {
            tiles[x][y] = Tile.FLOOR;
        }
    }

    public void addAtEmptyLocation(Creature creature) {
        int x;
        int y;

        do {
            x = (int) (Math.random() * this.width);
            y = (int) (Math.random() * this.height);
        } while (!tile(x, y).isGround() || this.creature(x, y) != null);

        creature.setX(x);
        creature.setY(y);

        this.creatures.add(creature);
    }

    public void addBonusAtEmptyLocation(Bonus bonus) {
        int x;
        int y;

        do {
            x = (int) (Math.random() * this.width);
            y = (int) (Math.random() * this.height);
        } while (!tile(x, y).isGround() || this.creature(x, y) != null);

        bonus.setX(x);
        bonus.setY(y);

        this.bonuses.add(bonus);
    }

    public void addBullet(Bullet bullet) {
        this.bullets.add(bullet);
    }

    public void addAtLocation(Creature creature, int x, int y) {
        creature.setX(x);
        creature.setY(y);
        this.creatures.add(creature);
    }

    public Creature creature(int x, int y) {
        for (Creature c : this.creatures) {
            if (c.x() == x && c.y() == y) {
                return c;
            }
        }
        return null;
    }

    public Bonus bonus(int x, int y) {
        for (Bonus b : this.bonuses)
            if (b.x() == x && b.y() == y)
                return b;
        return null;
    }

    public Bullet bullet(int x, int y) {
        for (Bullet b : this.bullets)
            if (b.x() == x && b.y() == y)
                return b;
        return null;
    }

    public ConcurrentLinkedDeque<Creature> getCreatures() {
        return this.creatures;
    }

    public List<Bonus> getBonuses() {
        return this.bonuses;
    }

    public ConcurrentLinkedDeque<Bullet> getBullets() {
        return this.bullets;
    }

    public void remove(Creature target) {
        this.creatures.remove(target);
    }

    public void removeBonus(Bonus target) {
        this.bonuses.remove(target);
    }

    public void removeBullet(Bullet target) {
        if (target != null)
            this.bullets.remove(target);
    }

    public void update() {
        ArrayList<Creature> toUpdate = new ArrayList<>(this.creatures);

        for (Creature creature : toUpdate) {
            creature.update();
        }
    }
}
