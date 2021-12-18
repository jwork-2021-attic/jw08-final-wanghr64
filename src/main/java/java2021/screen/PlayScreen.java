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
package java2021.screen;

import java2021.world.*;
import java2021.asciiPanel.AsciiPanel;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Aeranythe Echosong
 */
public class PlayScreen implements Screen, Serializable, Runnable {

    private World world;
    private Player player;
    private int screenWidth;
    private int screenHeight;
    private List<String> messages;
    private List<String> oldMessages;
    private PlayerAI[] myAIs;

    private int iCurAI;
    private int preDirect;
    private Date preMessageClearTime;

    public World world() {
        return this.world;
    }

    @Override
    public void run() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (int i = 1; i < 7; ++i)
                    if (player.curCoolTime[i] < player.maxCoolTime[i])
                        ++player.curCoolTime[i];
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }, 0, 1000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Random rd = new Random();
                if (rd.nextInt(2) == 0) {
                    Creature enemy = new Creature(world, (char) 15, AsciiPanel.fromPic, 15, 20, 5, 9);
                    new Thread(new BulletEnemyAI(enemy, world, player)).start();
                    world.addAtEmptyLocation(enemy);
                } else {
                    Creature enemy = new Creature(world, (char) 15, AsciiPanel.fromPic, 1, 20, 5, 9);
                    new Thread(new BombEnemyAI(enemy, world, player)).start();
                    world.addAtEmptyLocation(enemy);
                }
                world.addBonusAtEmptyLocation(new Bonus(world, rd.nextInt(3)));
                boolean finished = true;
                for (int i = 0; i < 7; ++i)
                    if (player.validAIs[i] == false)
                        finished = false;
                if (finished) {
                    world.addBonusAtEmptyLocation(new Bonus(world, 999));
                }
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                }
            }
        }, 5000, 5000);
    }

    public PlayScreen() {
        this.screenWidth = 38;
        this.screenHeight = 15;
        createWorld();
        this.messages = new ArrayList<String>();
        this.oldMessages = new ArrayList<String>();

        createCreatures();
        createBonusus();
    }

    private void createCreatures() {
        player = new Player(this.world, (char) 138, AsciiPanel.fromPic, 100, 20, 10, 9);
        world.addAtEmptyLocation(player);
        myAIs = new PlayerAI[7];
        myAIs[0] = new OldManAI(player, world, messages);
        myAIs[1] = new PowerBrotherAI(player, world, messages);
        myAIs[2] = new ViewBrotherAI(player, world, messages);
        myAIs[3] = new FireBrotherAI(player, world, messages);
        myAIs[4] = new WaterBrotherAI(player, world, messages);
        myAIs[5] = new SteelBrotherAI(player, world, messages);
        myAIs[6] = new HideBrotherAI(player, world, messages);
        iCurAI = 0;
        player.setAI(myAIs[iCurAI]);

        for (int i = 0; i < 10; ++i) {
            Creature enemy = new Creature(this.world, (char) 15, AsciiPanel.fromPic, 15, 20, 5, 9);
            new Thread(new BulletEnemyAI(enemy, world, player)).start();
            world.addAtEmptyLocation(enemy);
        }
        for (int i = 0; i < 10; ++i) {
            Creature enemy = new Creature(this.world, (char) 15, AsciiPanel.fromPic, 1, 20, 5, 9);
            new Thread(new BombEnemyAI(enemy, world, player)).start();
            world.addAtEmptyLocation(enemy);
        }
    }

    private void createBonusus() {
        for (int i = 0; i < 10; ++i) {
            world.addBonusAtEmptyLocation(new Bonus(world, 0));
            world.addBonusAtEmptyLocation(new Bonus(world, 1));
            world.addBonusAtEmptyLocation(new Bonus(world, 2));
        }

        for (int i = 11; i <= 16; ++i)
            world.addBonusAtEmptyLocation(new Bonus(world, i));
    }

    private void createWorld() {
        world = new WorldBuilder(90, 60).makeCaves().build();
    }

    private void displayTiles(AsciiPanel terminal, int left, int top) {
        // Show terrain
        for (int x = 0; x < screenWidth; x++) {
            for (int y = 0; y < screenHeight; y++) {
                int wx = x + left;
                int wy = y + top;

                if (player.canSee(wx, wy)) {
                    terminal.write(world.glyph(wx, wy), x, y, world.color(wx, wy));
                } else {
                    terminal.write((char) 0, x, y, AsciiPanel.fromPic);
                }
            }
        }
        // Show creatures
        for (Creature creature : world.getCreatures()) {
            if (creature.x() >= left && creature.x() < left + screenWidth && creature.y() >= top
                    && creature.y() < top + screenHeight) {
                if (player.canSee(creature.x(), creature.y())) {
                    terminal.write(creature.glyph(), creature.x() - left, creature.y() - top, AsciiPanel.fromPic);
                }
            }
        }

        // Show bonuses
        for (Bonus bonus : world.getBonuses()) {
            if (bonus.x() >= left && bonus.x() < left + screenWidth && bonus.y() >= top
                    && bonus.y() < top + screenHeight) {
                if (player.canSee(bonus.x(), bonus.y())) {
                    terminal.write(bonus.glyph(), bonus.x() - left, bonus.y() - top, AsciiPanel.fromPic);
                }
            }
        }

        // Show bullets
        for (Bullet bullet : world.getBullets()) {
            if (bullet.x() >= left && bullet.x() < left + screenWidth && bullet.y() >= top
                    && bullet.y() < top + screenHeight) {
                if (player.canSee(bullet.x(), bullet.y())) {
                    terminal.write(bullet.glyph(), bullet.x() - left, bullet.y() - top, AsciiPanel.fromPic);
                }
            }
        }

        // Creatures can choose their next action now
        world.update();
    }

    private void displayCoolTime(AsciiPanel terminal) {
        // Show characters
        for (int i = 1; i < 7; ++i) {
            if (!player.validAIs[i])
                continue;
            int leftStart = 5 + 11 * ((i - 1) % 3);
            int height = 18 + (i - 1) / 3;
            for (int j = 0; j < player.curCoolTime[i] / 2; ++j) {
                terminal.write('*', leftStart + j, height, AsciiPanel.brightGreen);
            }
        }
    }

    private void displaySkill(AsciiPanel terminal) {
        switch (iCurAI) {
            case 1:
                switch (preDirect) {
                    case KeyEvent.VK_LEFT:
                        terminal.write("*", player.x() - getScrollX() - 1, player.y() - getScrollY(), Color.RED);
                        break;
                    case KeyEvent.VK_RIGHT:
                        terminal.write("*", player.x() - getScrollX() + 1, player.y() - getScrollY(), Color.RED);
                        break;
                    case KeyEvent.VK_UP:
                        terminal.write("*", player.x() - getScrollX(), player.y() - getScrollY() - 1, Color.RED);
                        break;
                    case KeyEvent.VK_DOWN:
                        terminal.write("*", player.x() - getScrollX(), player.y() - getScrollY() + 1, Color.RED);
                        break;
                }
                break;
            case 2:
                break;
            case 3:
                switch (preDirect) {
                    case KeyEvent.VK_LEFT:
                        for (int i = 1; i < 6 && player.x() - getScrollX() - i >= 0
                                && world.tile(player.x() - i, player.y()) != Tile.WALL; ++i)
                            terminal.write((char) 161, player.x() - getScrollX() - i, player.y() - getScrollY(),
                                    AsciiPanel.fromPic);
                        break;
                    case KeyEvent.VK_RIGHT:
                        for (int i = 1; i < 6 && player.x() - getScrollX() + i < screenWidth
                                && world.tile(player.x() + i, player.y()) != Tile.WALL; ++i)
                            terminal.write((char) 163, player.x() - getScrollX() + i, player.y() - getScrollY(),
                                    AsciiPanel.fromPic);
                        break;
                    case KeyEvent.VK_UP:
                        for (int i = 1; i < 6 && player.y() - getScrollY() - i >= 0
                                && world.tile(player.x(), player.y() - i) != Tile.WALL; ++i)
                            terminal.write((char) 162, player.x() - getScrollX(), player.y() - getScrollY() - i,
                                    AsciiPanel.fromPic);
                        break;
                    case KeyEvent.VK_DOWN:
                        for (int i = 1; i < 6 && player.y() - getScrollY() + i < screenHeight
                                && world.tile(player.x(), player.y() + i) != Tile.WALL; ++i)
                            terminal.write((char) 160, player.x() - getScrollX(), player.y() - getScrollY() + i,
                                    AsciiPanel.fromPic);
                        break;
                }
                break;
            case 4:
                int r = 5;
                int xx = player.x() - getScrollX();
                int yy = player.y() - getScrollY();
                int wxx = player.x();
                int wyy = player.y();
                for (int i = 0; i < r; ++i) {
                    int rr = r - i;
                    for (int j = 0; j < rr; ++j) {
                        if (i == 0 && j == 0)
                            continue;
                        try {
                            if (xx + j < screenWidth && xx + j >= 0 && yy + i < screenHeight && yy + i >= 0
                                    && world.tile(wxx + j, wyy + i) != Tile.WALL)
                                terminal.write((char) 164, xx + j, yy + i, AsciiPanel.fromPic);
                        } catch (Exception e) {
                        }
                        try {
                            if (xx + j < screenWidth && xx + j >= 0 && yy - i < screenHeight && yy - i >= 0
                                    && world.tile(wxx + j, wyy - i) != Tile.WALL)
                                terminal.write((char) 164, xx + j, yy - i, AsciiPanel.fromPic);
                        } catch (Exception e) {
                        }
                        try {
                            if (xx - j < screenWidth && xx - j >= 0 && yy + i < screenHeight && yy + i >= 0
                                    && world.tile(wxx - j, wyy + i) != Tile.WALL)
                                terminal.write((char) 164, xx - j, yy + i, AsciiPanel.fromPic);
                        } catch (Exception e) {
                        }
                        try {
                            if (xx - j < screenWidth && xx - j >= 0 && yy - i < screenHeight && yy - i >= 0
                                    && world.tile(wxx - j, wyy - i) != Tile.WALL)
                                terminal.write((char) 164, xx - j, yy - i, AsciiPanel.fromPic);
                        } catch (Exception e) {
                        }

                    }
                }
                break;
            case 5:
                break;
            case 6:
                break;
            default:
                break;
        }
    }

    private void displayMessages(AsciiPanel terminal, List<String> messages) {
        if (preMessageClearTime == null)
            preMessageClearTime = new Date();
        int top = Math.max(this.screenHeight - messages.size(), this.screenHeight - 3);
        for (int i = 0; i < messages.size() && i < 3; i++) {
            terminal.write(messages.get(i), 1, top - i + 1);
        }
        this.oldMessages.addAll(messages);
        if (new Date().getTime() - preMessageClearTime.getTime() > 2000) {
            messages.clear();
            preMessageClearTime = null;
        }
    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        // Terrain and creatures
        displayTiles(terminal, getScrollX(), getScrollY());
        // Player
        terminal.write(player.glyph(), player.x() - getScrollX(), player.y() - getScrollY(), player.color());
        // Stats
        String stats = String.format("%3d/%3d hp %2d digs", player.hp(),
                player.maxHP(), player.digCount);
        terminal.write(stats, 1, 16);
        // Messages
        // displayMessages(terminal, this.messages);
        // Cool Times
        displayCoolTime(terminal);

        if (player.onSkill())
            displaySkill(terminal);

        if (player.validAIs[1])
            terminal.write((char) 144, 4, 18, AsciiPanel.fromPic);
        if (player.validAIs[2])
            terminal.write((char) 145, 15, 18, AsciiPanel.fromPic);
        if (player.validAIs[3])
            terminal.write((char) 146, 26, 18, AsciiPanel.fromPic);
        if (player.validAIs[4])
            terminal.write((char) 147, 4, 19, AsciiPanel.fromPic);
        if (player.validAIs[5])
            terminal.write((char) 148, 15, 19, AsciiPanel.fromPic);
        if (player.validAIs[6])
            terminal.write((char) 149, 26, 19, AsciiPanel.fromPic);

        switch (iCurAI) {
            case 1:
                terminal.write('*', 3, 18, AsciiPanel.red);
                break;
            case 2:
                terminal.write('*', 14, 18, AsciiPanel.red);
                break;
            case 3:
                terminal.write('*', 25, 18, AsciiPanel.red);
                break;
            case 4:
                terminal.write('*', 3, 19, AsciiPanel.red);
                break;
            case 5:
                terminal.write('*', 14, 19, AsciiPanel.red);
                break;
            case 6:
                terminal.write('*', 25, 19, AsciiPanel.red);
                break;
        }
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        if (player.freeze())
            return this;
        if (player.win)
            return new WinScreen();
        if (player.hp() <= 0)
            return new LoseScreen();
        switch (key.getKeyCode()) {
            case KeyEvent.VK_A:
                player.moveBy(-1, 0);
                preDirect = KeyEvent.VK_LEFT;
                break;
            case KeyEvent.VK_D:
                player.moveBy(1, 0);
                preDirect = KeyEvent.VK_RIGHT;
                break;
            case KeyEvent.VK_W:
                player.moveBy(0, -1);
                preDirect = KeyEvent.VK_UP;
                break;
            case KeyEvent.VK_S:
                player.moveBy(0, 1);
                preDirect = KeyEvent.VK_DOWN;
                break;
            case KeyEvent.VK_Q:
                if (!player.onSkill()) {
                    iCurAI = iCurAI == 0 ? 6 : iCurAI - 1;
                    while (!player.validAIs[iCurAI])
                        iCurAI = iCurAI == 0 ? 6 : iCurAI - 1;
                    player.setAI(myAIs[iCurAI]);
                    if (iCurAI != 0)
                        player.setGlyph((char) (144 + iCurAI - 1));
                    else
                        player.setGlyph((char) 138);
                }
                // player.setColor(AsciiPanel.fromPic);
                break;
            case KeyEvent.VK_E:
                if (!player.onSkill()) {
                    iCurAI = (iCurAI + 1) % 7;
                    while (!player.validAIs[iCurAI])
                        iCurAI = (iCurAI + 1) % 7;
                    player.setAI(myAIs[iCurAI]);
                    if (iCurAI != 0)
                        player.setGlyph((char) (144 + iCurAI - 1));
                    else
                        player.setGlyph((char) 138);
                }
                // player.setColor(Player.id2Color(iCurAI));}
                break;
            case KeyEvent.VK_J:
                if (player.curCoolTime[iCurAI] >= player.costCoolTime[iCurAI]) {
                    player.skill();
                    player.curCoolTime[iCurAI] -= player.costCoolTime[iCurAI];
                }
                break;
            case KeyEvent.VK_ESCAPE:
                return new SaveScreen(this);
        }

        return this;
    }

    public int getScrollX() {
        return Math.max(0, Math.min(player.x() - screenWidth / 2, world.width() - screenWidth));
    }

    public int getScrollY() {
        return Math.max(0, Math.min(player.y() - screenHeight / 2, world.height() - screenHeight));
    }
}
