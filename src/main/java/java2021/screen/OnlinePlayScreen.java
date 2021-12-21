package java2021.screen;

import java.awt.event.KeyEvent;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.*;
import java.io.*;

import java.awt.Color;

import java2021.MyUtils;
import java2021.asciiPanel.AsciiPanel;
import java2021.server.Package2Client;
import java2021.world.*;

public class OnlinePlayScreen implements Screen, Runnable {

    private SocketChannel client;
    private ByteBuffer buffer;
    private int restLen;

    private World world;
    private Player player;

    private int screenWidth;
    private int screenHeight;

    public OnlinePlayScreen(String host, int port) {
        this.screenWidth = 38;
        this.screenHeight = 15;
        try {
            this.client = SocketChannel.open(new InetSocketAddress(host, port));
            buffer = ByteBuffer.allocate(8192);
            restLen = 0;

            while (true) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                buffer.clear();
                client.read(buffer);
                buffer.flip();
                if (restLen == 0) {
                    restLen = buffer.getInt();
                }
                while (restLen > 0 && buffer.remaining() > 0) {
                    bytes.write(buffer.get());
                    --restLen;
                }
                if (restLen == 0 && bytes.size() > 0) {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
                    Package2Client pkg = (Package2Client) ois.readObject();
                    System.out.println("GET package");
                    this.world = pkg.world();
                    this.player = pkg.player();
                    bytes.reset();
                    break;
                }
            }
        } catch (IOException e) {
            // TODO: implement server note found exception
        } catch (Exception e) {

        }

    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        if (player == null || world == null)
            return;
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

        switch (player.iCurAI) {
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
        try {
            client.write(ByteBuffer.wrap(MyUtils.int2byteArraryBigEnd(key.getKeyCode())));
        } catch (Exception e) {
        }
        return this;
    }

    @Override
    public void run() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            while (true) {
                buffer.clear();
                client.read(buffer);
                // System.out.println("READ, from " + client.socket());
                buffer.flip();
                if (restLen == 0) {
                    restLen = buffer.getInt();
                }
                while (restLen > 0 && buffer.remaining() > 0) {
                    bytes.write(buffer.get());
                    --restLen;
                }
                if (restLen == 0 && bytes.size() > 0) {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
                    Package2Client pkg = (Package2Client) ois.readObject();
                    System.out.println("GET package");
                    this.world = pkg.world();
                    this.player = pkg.player();
                    if (world == null | player == null) {
                        System.out.println("WEIRD!!");
                    }
                    bytes.reset();
                }
            }
        } catch (Exception e) {
        }
    }

    private int getScrollX() {
        return Math.max(0, Math.min(player.x() - screenWidth / 2, world.width() - screenWidth));
    }

    private int getScrollY() {
        return Math.max(0, Math.min(player.y() - screenHeight / 2, world.height() - screenHeight));
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
        switch (player.iCurAI) {
            case 1:
                switch (player.preDirect) {
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
                switch (player.preDirect) {
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
}
