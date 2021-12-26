package java2021.screen;

import java.awt.event.KeyEvent;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.nio.*;
import java.io.*;
import java.util.concurrent.*;
import java.awt.Color;

import java2021.MyUtils;
import java2021.asciiPanel.AsciiPanel;
import java2021.world.*;

public class OnlinePlayScreen implements Screen, Runnable {

    private SocketChannel client;

    private int myIndex;

    private int[] curCoolTime;
    private boolean[] validAIs;
    private boolean[] onSkill;
    private int[] preDirect;
    private int xx;
    private int yy;
    private int HP;
    private int[] icurAI;
    private int myCurAI;
    private int digCount;
    private int[][] playerPoints;
    private final int worldWidth = 90;
    private final int worldHeight = 60;
    private ConcurrentLinkedDeque<int[]> wall;
    private ConcurrentLinkedDeque<int[]> creature;
    private ConcurrentLinkedDeque<int[]> bonus;
    private ConcurrentLinkedDeque<int[]> bullet;

    private boolean won = false;
    private boolean lost = false;
    private boolean disconnected = false;
    private int wonIndex;

    private int screenWidth;
    private int screenHeight;

    public OnlinePlayScreen(String host, int port) throws Exception {
        curCoolTime = new int[7];
        validAIs = new boolean[7];
        onSkill = new boolean[4];
        preDirect = new int[4];
        icurAI = new int[4];
        playerPoints = new int[4][2];
        this.screenWidth = 38;
        this.screenHeight = 15;
        this.client = SocketChannel.open(new InetSocketAddress(host, port));
    }

    @Override
    public synchronized void displayOutput(AsciiPanel terminal) {
        if (wall == null || creature == null || bonus == null || bullet == null) {
            terminal.write("Wating Other Players...", 6, 15);
            return;
        }
        terminal.clear();
        // Terrain and creatures
        displayTiles(terminal, getScrollX(), getScrollY());
        // Stats
        String stats = String.format("%3d/%3d hp %2d digs", HP,
                100, digCount);
        terminal.write(stats, 1, 16);
        terminal.write(Integer.toString(myCurAI), 26, 16);
        terminal.write(onSkill[myIndex] ? "True" : "False", 28, 16);
        // Cool Times
        displayCoolTime(terminal);
        // Skills
        displaySkill(terminal);
        // valid & selected AIs
        displayAI(terminal);
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        if (lost)
            return new LoseScreen();
        if (won) {
            if (wonIndex == myIndex)
                return new WinScreen();
            else
                return new OtherWinScreen(wonIndex);
        }
        if (disconnected) {
            return new LostConnectionScreen();
        }
        try {
            client.write(ByteBuffer.wrap(MyUtils.int2byteArraryBigEnd(key.getKeyCode())));
        } catch (Exception e) {
            System.out.println(e);
        }
        return this;
    }

    @Override
    public void run() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ByteBuffer buffer = ByteBuffer.allocate(200000000);
            int restLen = 0;
            int type = 0;
            while (!(lost || won || disconnected)) {
                buffer.clear();
                client.read(buffer);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    if (restLen == 0) {
                        type = buffer.get();
                        // System.out.println(type);
                        switch (type) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                                myIndex = type;
                                restLen = 0;
                                break;
                            case 11:
                                restLen = 19;
                                break;
                            case 22:
                                restLen = buffer.getInt() * 2;
                                break;
                            case 33:
                                restLen = buffer.getInt() * 3;
                                break;
                            case 44:
                                restLen = buffer.getInt() * 3;
                                break;
                            case 55:
                                restLen = buffer.getInt() * 3;
                                break;
                            case 66:
                                restLen = 20;
                                break;
                            case 77:
                                wonIndex = buffer.get();
                                won = true;
                                break;
                            case 88:
                                lost = true;
                                break;
                            case 99:
                                disconnected = true;
                                break;
                        }
                    }
                    if (won || lost || disconnected)
                        break;
                    while (restLen > 0 && buffer.remaining() > 0) {
                        bytes.write(buffer.get());
                        --restLen;
                    }
                    if (restLen == 0 && bytes.size() > 0) {
                        switch (type) {
                            case 11:
                                infoParse(bytes.toByteArray());
                                break;
                            case 22:
                                wall = twoBytes2List(bytes.toByteArray());
                                break;
                            case 33:
                                creature = threeBytes2List(bytes.toByteArray());
                                break;
                            case 44:
                                bonus = threeBytes2List(bytes.toByteArray());
                                break;
                            case 55:
                                bullet = threeBytes2List(bytes.toByteArray());
                                break;
                            case 66:
                                skillParse(bytes.toByteArray());
                                break;
                        }
                        bytes.reset();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            for (int i = 0; i < 5 && i < e.getStackTrace().length; ++i)
                System.out.println("\t" + e.getStackTrace()[i]);
            new Thread(this).start();
        }
    }

    private static ConcurrentLinkedDeque<int[]> twoBytes2List(byte[] arr) {
        ConcurrentLinkedDeque<int[]> res = new ConcurrentLinkedDeque<>();
        for (int i = 0; i < arr.length; i += 2) {
            res.add(new int[] { Byte.toUnsignedInt(arr[i]), Byte.toUnsignedInt(arr[i + 1]) });
        }
        return res;
    }

    private static ConcurrentLinkedDeque<int[]> threeBytes2List(byte[] arr) {
        ConcurrentLinkedDeque<int[]> res = new ConcurrentLinkedDeque<>();
        for (int i = 0; i < arr.length; i += 3) {
            res.add(new int[] { Byte.toUnsignedInt(arr[i]), Byte.toUnsignedInt(arr[i + 1]),
                    Byte.toUnsignedInt(arr[i + 2]) });

        }
        return res;
    }

    private void infoParse(byte[] arr) {
        xx = Byte.toUnsignedInt(arr[0]);
        yy = Byte.toUnsignedInt(arr[1]);
        HP = Byte.toUnsignedInt(arr[2]);
        digCount = Byte.toUnsignedInt(arr[3]);
        myCurAI = Byte.toUnsignedInt(arr[4]);
        for (int i = 0; i < 7; ++i) {
            validAIs[i] = Byte.toUnsignedInt(arr[i + 5]) == 1;
            curCoolTime[i] = Byte.toUnsignedInt(arr[i + 12]);
        }
    }

    private void skillParse(byte[] arr) {
        for (int i = 0; i < 4; ++i) {
            playerPoints[i][0] = Byte.toUnsignedInt(arr[2 * i]);
            playerPoints[i][1] = Byte.toUnsignedInt(arr[2 * i + 1]);
        }
        for (int i = 0; i < 4; ++i) {
            icurAI[i] = Byte.toUnsignedInt(arr[i + 8]);
            preDirect[i] = Byte.toUnsignedInt(arr[i + 12]);
            onSkill[i] = Byte.toUnsignedInt(arr[i + 16]) == 1;
        }

    }

    private int getScrollX() {
        return Math.max(0, Math.min(xx - screenWidth / 2, worldWidth - screenWidth));
    }

    private int getScrollY() {
        return Math.max(0, Math.min(yy - screenHeight / 2, worldHeight - screenHeight));
    }

    private boolean canSee(int x, int y) {
        if (onSkill[myIndex] && myCurAI == 2)
            return true;
        else
            return (xx - x) * (xx - x) + (yy - y) * (yy - y) <= 9
                    * 9;
    }

    private void displayAI(AsciiPanel terminal) {
        if (validAIs[1])
            terminal.write((char) 144, 4, 18, AsciiPanel.fromPic);
        if (validAIs[2])
            terminal.write((char) 145, 15, 18, AsciiPanel.fromPic);
        if (validAIs[3])
            terminal.write((char) 146, 26, 18, AsciiPanel.fromPic);
        if (validAIs[4])
            terminal.write((char) 147, 4, 19, AsciiPanel.fromPic);
        if (validAIs[5])
            terminal.write((char) 148, 15, 19, AsciiPanel.fromPic);
        if (validAIs[6])
            terminal.write((char) 149, 26, 19, AsciiPanel.fromPic);

        switch (myCurAI) {
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

    private void displayTiles(AsciiPanel terminal, int left, int top) {
        // Show terrain
        for (int x = 0; x < screenWidth; x++)
            for (int y = 0; y < screenHeight; y++) {
                int wx = x + left;
                int wy = y + top;
                if (canSee(wx, wy))
                    terminal.write(Tile.FLOOR.glyph(), x, y, AsciiPanel.fromPic);
                else
                    terminal.write((char) 0, x, y, AsciiPanel.fromPic);
            }
        for (int[] p : wall) {
            int x = p[0];
            int y = p[1];
            if (x >= left && x < left + screenWidth && y >= top
                    && y < top + screenHeight && canSee(p[0], p[1]))
                terminal.write(Tile.WALL.glyph(), x - left, y - top, AsciiPanel.fromPic);
        }

        // Show bonuses
        for (int[] p : bonus) {
            char glyph = (char) p[0];
            int x = p[1];
            int y = p[2];
            if (x >= left && x < left + screenWidth && y >= top
                    && y < top + screenHeight && canSee(x, y))
                terminal.write(glyph, x - left, y - top, AsciiPanel.fromPic);
        }

        // Show bullets
        for (int[] p : bonus) {
            char glyph = (char) p[0];
            int x = p[1];
            int y = p[2];
            if (x >= left && x < left + screenWidth && y >= top
                    && y < top + screenHeight && canSee(x, y))
                terminal.write(glyph, x - left, y - top, AsciiPanel.fromPic);
        }

        // Show creatures
        for (int[] p : creature) {
            char glyph = (char) ((int) p[0]);
            int x = p[1];
            int y = p[2];
            if (x >= left && x < left + screenWidth && y >= top
                    && y < top + screenHeight && canSee(x, y))
                terminal.write(glyph, x - left, y - top, AsciiPanel.fromPic);
        }
    }

    private void displayCoolTime(AsciiPanel terminal) {
        // Show characters
        for (int i = 1; i < 7; ++i) {
            if (!validAIs[i])
                continue;
            int leftStart = 5 + 11 * ((i - 1) % 3);
            int height = 18 + (i - 1) / 3;
            for (int j = 0; j < curCoolTime[i] / 2; ++j) {
                terminal.write('*', leftStart + j, height, AsciiPanel.brightGreen);
            }
        }
    }

    private void displaySkill(AsciiPanel terminal) {
        for (int kkk = 0; kkk < 4; ++kkk)
            if (onSkill[kkk])
                switch (icurAI[kkk]) {
                    case 1:
                        switch (preDirect[kkk]) {
                            case KeyEvent.VK_LEFT:
                                terminal.write("*", playerPoints[kkk][0] - getScrollX() - 1,
                                        playerPoints[kkk][1] - getScrollY(),
                                        Color.RED);
                                break;
                            case KeyEvent.VK_RIGHT:
                                terminal.write("*", playerPoints[kkk][0] - getScrollX() + 1,
                                        playerPoints[kkk][1] - getScrollY(),
                                        Color.RED);
                                break;
                            case KeyEvent.VK_UP:
                                terminal.write("*", playerPoints[kkk][0] - getScrollX(),
                                        playerPoints[kkk][1] - getScrollY() - 1,
                                        Color.RED);
                                break;
                            case KeyEvent.VK_DOWN:
                                terminal.write("*", playerPoints[kkk][0] - getScrollX(),
                                        playerPoints[kkk][1] - getScrollY() + 1,
                                        Color.RED);
                                break;
                        }
                        break;
                    case 2:
                        break;
                    case 3:
                        switch (preDirect[kkk]) {
                            case KeyEvent.VK_LEFT:
                                for (int i = 1; i < 6 && playerPoints[kkk][0] - getScrollX() - i >= 0; ++i)
                                    terminal.write((char) 161, playerPoints[kkk][0] - getScrollX() - i,
                                            playerPoints[kkk][1] - getScrollY(),
                                            AsciiPanel.fromPic);
                                break;
                            case KeyEvent.VK_RIGHT:
                                for (int i = 1; i < 6 && playerPoints[kkk][0] - getScrollX() + i < screenWidth; ++i)
                                    terminal.write((char) 163, playerPoints[kkk][0] - getScrollX() + i,
                                            playerPoints[kkk][1] - getScrollY(),
                                            AsciiPanel.fromPic);
                                break;
                            case KeyEvent.VK_UP:
                                for (int i = 1; i < 6 && playerPoints[kkk][1] - getScrollY() - i >= 0; ++i)
                                    terminal.write((char) 162, playerPoints[kkk][0] - getScrollX(),
                                            playerPoints[kkk][1] - getScrollY() - i,
                                            AsciiPanel.fromPic);
                                break;
                            case KeyEvent.VK_DOWN:
                                for (int i = 1; i < 6 && playerPoints[kkk][1] - getScrollY() + i < screenHeight; ++i)
                                    terminal.write((char) 160, playerPoints[kkk][0] - getScrollX(),
                                            playerPoints[kkk][1] - getScrollY() + i,
                                            AsciiPanel.fromPic);
                                break;
                        }
                        break;
                    case 4:
                        int r = 5;
                        int xx = playerPoints[kkk][0] - getScrollX();
                        int yy = playerPoints[kkk][1] - getScrollY();
                        for (int i = 0; i < r; ++i) {
                            int rr = r - i;
                            for (int j = 0; j < rr; ++j) {
                                if (i == 0 && j == 0)
                                    continue;
                                try {
                                    if (xx + j < screenWidth && xx + j >= 0 && yy + i < screenHeight && yy + i >= 0)
                                        terminal.write((char) 164, xx + j, yy + i, AsciiPanel.fromPic);
                                } catch (Exception e) {
                                    System.out.println(e);
                                }
                                try {
                                    if (xx + j < screenWidth && xx + j >= 0 && yy - i < screenHeight && yy - i >= 0)
                                        terminal.write((char) 164, xx + j, yy - i, AsciiPanel.fromPic);
                                } catch (Exception e) {
                                    System.out.println(e);
                                }
                                try {
                                    if (xx - j < screenWidth && xx - j >= 0 && yy + i < screenHeight && yy + i >= 0)
                                        terminal.write((char) 164, xx - j, yy + i, AsciiPanel.fromPic);
                                } catch (Exception e) {
                                    System.out.println(e);
                                }
                                try {
                                    if (xx - j < screenWidth && xx - j >= 0 && yy - i < screenHeight && yy - i >= 0)
                                        terminal.write((char) 164, xx - j, yy - i, AsciiPanel.fromPic);
                                } catch (Exception e) {
                                    System.out.println(e);
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
