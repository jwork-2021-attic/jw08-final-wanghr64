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
package java2021.world;

import java.awt.Point;

/**
 *
 * @author Aeranythe Echosong
 */
abstract class CreatureAI implements Runnable {

    protected Creature creature;
    protected World world;

    public CreatureAI(Creature creature, World world) {
        this.creature = creature;
        this.world = world;
        this.creature.setAI(this);
    }

    protected boolean onSkill;
    protected boolean freeze;

    public boolean onSkill() {
        return this.onSkill;
    }

    public boolean freeze() {
        return this.freeze;
    }

    public void onEnter(int x, int y, Tile tile) {
    }

    public void onUpdate() {
    }

    public void onNotify(String message) {
    }

    public boolean canSee(int x, int y) {
        if ((creature.x() - x) * (creature.x() - x) + (creature.y() - y) * (creature.y() - y) > creature.visionRadius()
                * creature.visionRadius()) {
            return false;
        }
        for (Point p : new Line(creature.x(), creature.y(), x, y)) {
            if (creature.tile(p.x, p.y).isGround() || (p.x == x && p.y == y)) {
                continue;
            }
            return false;
        }
        return true;
    }
}
