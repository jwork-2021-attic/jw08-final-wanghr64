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

import java.awt.Color;

/**
 *
 * @author Aeranythe Echosong
 */
public class Creature {

    private World world;

    private int x;

    public void setX(int x) {
        this.x = x;
    }

    public int x() {
        return x;
    }

    private int y;

    public void setY(int y) {
        this.y = y;
    }

    public int y() {
        return y;
    }

    private char glyph;

    public char glyph() {
        return this.glyph;
    }

    public void setGlyph(char glyph) {
        this.glyph = glyph;
    }

    private Color color;

    public Color color() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    protected CreatureAI ai;

    public void setAI(CreatureAI ai) {
        this.ai = ai;
    }

    private int maxHP;

    public int maxHP() {
        return this.maxHP;
    }

    private int hp;

    public int hp() {
        return this.hp;
    }

    public boolean modifyHP(int amount) {
        this.hp += amount;

        if (this.hp < 1) {
            world.remove(this);
            return true;
        }

        return false;
    }

    private int attackValue;

    public int attackValue() {
        return this.attackValue;
    }

    protected int defenseValue;

    public int defenseValue() {
        return this.defenseValue;
    }

    private int visionRadius;

    public int visionRadius() {
        return this.visionRadius;
    }

    public boolean canSee(int wx, int wy) {
        return ai.canSee(wx, wy);
    }

    public Tile tile(int wx, int wy) {
        return world.tile(wx, wy);
    }

    public void dig(int wx, int wy) {
        world.dig(wx, wy);
    }

    public int direction = 0;// 0:left 1:right 2:up 3:down

    public boolean moveBy(int mx, int my) {
        if (mx < 0)
            direction = 0;
        else if (mx > 0)
            direction = 1;
        else if (my < 0)
            direction = 2;
        else
            direction = 3;
        Creature other = world.creature(x + mx, y + my);
        Bonus bonus = world.bonus(x + mx, y + my);

        if (PlayerAI.class.isAssignableFrom(this.ai.getClass()) && bonus != null) {
            ((PlayerAI) this.ai).getBonus(bonus);
            this.notify("I get a bonus " + bonus.type() + ", LUCKY!");
            bonus.remove();
        }
        if (other == null) {
            ai.onEnter(x + mx, y + my, world.tile(x + mx, y + my));
            return true;
        }
        return false;
    }

    public void attack(Creature other) {
        if (other == null)
            return;
        int damage = Math.max(0, this.attackValue() - other.defenseValue());

        boolean killed = other.modifyHP(-damage);

        this.notify("You attack the '%s' for %d damage.", other.glyph, damage);
        other.notify("The '%s' attacks you for %d damage.", glyph, damage);

        if (killed)
            this.notify("You KILLED '%s' !", other.glyph);
    }

    public void update() {
        this.ai.onUpdate();
    }

    public boolean canEnter(int x, int y) {
        return world.tile(x, y).isGround();
    }

    public void notify(String message, Object... params) {
        ai.onNotify(String.format(message, params));
    }

    public Creature(World world, char glyph, Color color, int maxHP, int attack, int defense, int visionRadius) {
        this.world = world;
        this.glyph = glyph;
        this.color = color;
        this.maxHP = maxHP;
        this.hp = maxHP;
        this.attackValue = attack;
        this.defenseValue = defense;
        this.visionRadius = visionRadius;
    }
}
