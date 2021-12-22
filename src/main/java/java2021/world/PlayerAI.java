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
import java.util.*;

/**
 *
 * @author Aeranythe Echosong
 */
public abstract class PlayerAI extends CreatureAI {

    private List<String> messages;

    protected Player player = (Player) creature;

    protected boolean allSee;

    public PlayerAI(Creature creature, World world, List<String> messages) {
        super(creature, world);
        this.messages = messages;
    }

    public void onEnter(int x, int y, Tile tile) {
        if (tile.isGround()) {
            creature.setX(x);
            creature.setY(y);
        } else if (tile.isDiggable() && player.digCount > 0) {
            creature.dig(x, y);
            --player.digCount;
        }
    }


    public void getBonus(Bonus bonus) {
        switch (bonus.type()) {
            case 0:// dig
                player.digCount += 10;
                break;
            case 1:// hp
                int diffHP = Math.min(10, 100 - player.hp());
                player.modifyHP(diffHP);
                break;
            case 2:// ct
                for (int i = 1; i < 7; ++i)
                    player.curCoolTime[i] = 10;
                break;
            case 11:// 1
                player.validAIs[1] = true;
                break;
            case 12:// 2
                player.validAIs[2] = true;
                break;
            case 13:// 3
                player.validAIs[3] = true;
                break;
            case 14:// 4
                player.validAIs[4] = true;
                break;
            case 15:// 5
                player.validAIs[5] = true;
                break;
            case 16:// 6
                player.validAIs[6] = true;
                break;
            case 999:
                player.win = true;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean canSee(int x, int y) {
        if (allSee)
            return true;
        if ((creature.x() - x) * (creature.x() - x) + (creature.y() - y) * (creature.y() - y) > creature.visionRadius()
                * creature.visionRadius()) {
            return false;
        }
        for (Point p : new Line(player.x(), player.y(), x, y)) {
            if (creature.tile(p.x, p.y).isGround() || (p.x == x && p.y == y)) {
                continue;
            }
            return false;
        }
        return true;
    }

}