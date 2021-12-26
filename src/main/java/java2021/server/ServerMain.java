package java2021.server;

import java.nio.*;
import java.nio.channels.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

import java2021.world.*;
import java2021.MyUtils;
import java2021.asciiPanel.*;

class MyServer {

    private World world;
    private Player[] players;
    private PlayerAI[][] pAIs;
    private Random rd;
    private boolean[] won;
    private Timer coolTimeUpdate;
    private Timer generateEnemyBonus;

    MyServer() {
        players = new Player[4];
        pAIs = new PlayerAI[4][7];
        won = new boolean[4];
        rd = new Random();
        createWorld();
        createPlayers();
        createEnemies();
        createBonuses();/*
                         * coolTimeUpdate = new Timer();
                         * coolTimeUpdate.schedule(new TimerTask() {
                         * 
                         * @Override
                         * public void run() {
                         * for (int index = 0; index < 4; ++index) {
                         * Player player = players[index];
                         * for (int i = 1; i < 7; ++i)
                         * if (player.curCoolTime[i] < player.maxCoolTime[i])
                         * ++player.curCoolTime[i];
                         * }
                         * try {
                         * } catch (Exception e) {
                         * System.out.println(e);
                         * }
                         * }
                         * }, 0, 1000);
                         * generateEnemyBonus = new Timer();
                         * generateEnemyBonus.schedule(new TimerTask() {
                         * 
                         * @Override
                         * public void run() {
                         * Random rd = new Random();
                         * if (rd.nextInt(2) == 0) {
                         * Creature enemy = new Creature(world, (char) 15, AsciiPanel.fromPic, 15, 20,
                         * 5, 9);
                         * new Thread(new BulletEnemyAI(enemy, world, players[rd.nextInt(4)])).start();
                         * world.addAtEmptyLocation(enemy);
                         * } else {
                         * Creature enemy = new Creature(world, (char) 15, AsciiPanel.fromPic, 1, 20, 5,
                         * 9);
                         * new Thread(new BombEnemyAI(enemy, world, players[rd.nextInt(4)])).start();
                         * world.addAtEmptyLocation(enemy);
                         * }
                         * world.addBonusAtEmptyLocation(new Bonus(world, rd.nextInt(3)));
                         * for (int index = 0; index < 4; ++index)
                         * for (int i = 0; i < 7; ++i)
                         * if (players[index].validAIs[i] == false)
                         * won[index] = false;
                         * for (int index = 0; index < 4; ++index)
                         * if (won[index]) {
                         * world.addBonusAtEmptyLocation(new Bonus(world, 100 + index));
                         * }
                         * try {
                         * } catch (Exception e) {
                         * System.out.println(e);
                         * }
                         * }
                         * }, 3000, 3000);
                         */
    }

    private void createWorld() {
        world = new WorldBuilder(90, 60).makeCaves().build();
    }

    private void createPlayers() {
        for (int i = 0; i < 4; ++i) {
            players[i] = new Player(i, this.world, (char) 138, AsciiPanel.fromPic, 100, 20, 500, 9);
            world.addAtEmptyLocation(players[i]);
            pAIs[i][0] = new OldManAI(players[i], world);
            pAIs[i][1] = new PowerBrotherAI(players[i], world);
            pAIs[i][2] = new ViewBrotherAI(players[i], world);
            pAIs[i][3] = new FireBrotherAI(players[i], world);
            pAIs[i][4] = new WaterBrotherAI(players[i], world);
            pAIs[i][5] = new SteelBrotherAI(players[i], world);
            pAIs[i][6] = new HideBrotherAI(players[i], world);
        }
    }

    private void createEnemies() {
        for (int i = 0; i < 10; ++i) {
            Creature enemy = new Creature(this.world, (char) 15, AsciiPanel.fromPic, 15, 20, 5, 9);
            new Thread(new BulletEnemyAI(enemy, world, players[rd.nextInt(4)])).start();
            world.addAtEmptyLocation(enemy);
        }
        for (int i = 0; i < 10; ++i) {
            Creature enemy = new Creature(this.world, (char) 15, AsciiPanel.fromPic, 1, 20, 5, 9);
            new Thread(new BombEnemyAI(enemy, world, players[rd.nextInt(4)])).start();
            world.addAtEmptyLocation(enemy);
        }
    }

    private void createBonuses() {
        for (int i = 0; i < 10; ++i) {
            world.addBonusAtEmptyLocation(new Bonus(world, 0));
            world.addBonusAtEmptyLocation(new Bonus(world, 1));
            world.addBonusAtEmptyLocation(new Bonus(world, 2));
        }
        for (int index = 0; index < 4; ++index)
            for (int i = 1; i < 7; ++i)
                world.addBonusAtEmptyLocation(new Bonus(world, (index + 1) * 10 + i));
    }

    public byte[] getPackage(int index) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] res = null;
        try {
            int winIndex = -1;
            for (int i = 0; i < 4; ++i)
                if (players[i].win) {
                    winIndex = i;
                    break;
                }
            if (winIndex != -1) {
                baos.write(77);
                baos.write(winIndex);
            } else if (players[index].hp() <= 0) {
                baos.write(88);
            } else {
                baos.write(11);
                baos.write(this.info2Bytes(index));
                baos.write(22);
                baos.write(world.wall2Bytes());
                baos.write(33);
                baos.write(world.creature2Bytes());
                baos.write(44);
                baos.write(this.privateBonus2Bytes(index));
                baos.write(55);
                baos.write(world.bullet2Bytes());
                baos.write(66);
                baos.write(this.skill2Bytes());
            }
            res = baos.toByteArray();
            baos.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }

    public void handleKeyCode(int index, int KeyCode) {
        Player player = players[index];
        if (player.freeze())
            return;
        switch (KeyCode) {
            case KeyEvent.VK_A:
                player.moveBy(-1, 0);
                player.preDirect = KeyEvent.VK_LEFT;
                break;
            case KeyEvent.VK_D:
                player.moveBy(1, 0);
                player.preDirect = KeyEvent.VK_RIGHT;
                break;
            case KeyEvent.VK_W:
                player.moveBy(0, -1);
                player.preDirect = KeyEvent.VK_UP;
                break;
            case KeyEvent.VK_S:
                player.moveBy(0, 1);
                player.preDirect = KeyEvent.VK_DOWN;
                break;
            case KeyEvent.VK_Q:
                if (!player.onSkill()) {
                    player.iCurAI = player.iCurAI == 0 ? 6 : player.iCurAI - 1;
                    while (!player.validAIs[player.iCurAI])
                        player.iCurAI = player.iCurAI == 0 ? 6 : player.iCurAI - 1;
                    if (player.iCurAI != 0)
                        player.setGlyph((char) (144 + player.iCurAI - 1));
                    else
                        player.setGlyph((char) 138);
                }
                // player.setColor(AsciiPanel.fromPic);
                break;
            case KeyEvent.VK_E:
                if (!player.onSkill()) {
                    player.iCurAI = (player.iCurAI + 1) % 7;
                    while (!player.validAIs[player.iCurAI])
                        player.iCurAI = (player.iCurAI + 1) % 7;
                    if (player.iCurAI != 0)
                        player.setGlyph((char) (144 + player.iCurAI - 1));
                    else
                        player.setGlyph((char) 138);
                }
                break;
            case KeyEvent.VK_J:
                if (player.curCoolTime[player.iCurAI] >= player.costCoolTime[player.iCurAI]) {
                    player.skill();
                    player.curCoolTime[player.iCurAI] -= player.costCoolTime[player.iCurAI];
                }
                break;
        }
    }

    private byte[] info2Bytes(int index) {
        // 19 bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(players[index].x());
        baos.write(players[index].y());
        baos.write(players[index].hp());
        baos.write(players[index].digCount);
        baos.write(players[index].iCurAI);
        for (int i = 0; i < 7; ++i) {
            baos.write(players[index].validAIs[i] ? 1 : 0);
        }
        for (int i = 0; i < 7; ++i)
            baos.write(players[index].curCoolTime[i]);
        byte[] res = baos.toByteArray();
        try {
            baos.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }

    private byte[] skill2Bytes() {
        // 20 bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 4; ++i) {
            baos.write(players[i].x());
            baos.write(players[i].y());
        }
        for (int i = 0; i < 4; ++i)
            baos.write(players[i].iCurAI);
        for (int i = 0; i < 4; ++i)
            baos.write(players[i].preDirect);
        for (int i = 0; i < 4; ++i)
            baos.write(players[i].onSkill() ? 1 : 0);
        byte[] res = baos.toByteArray();
        try {
            baos.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }

    private byte[] privateBonus2Bytes(int index) {
        int count = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Bonus b : world.getBonuses()) {
            if (b.type() < 10 || b.type() - (index + 1) * 10 >= 1 && b.type() - (index + 1) * 10 < 7
                    || b.type() == 100 + index) {
                ++count;
                baos.write(b.glyph());
                baos.write(b.x());
                baos.write(b.y());
            }
        }
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        MyUtils.addInt2ByteArrayOS(temp, count);
        byte[] res = null;
        try {
            temp.write(baos.toByteArray());
            res = temp.toByteArray();
            baos.close();
            temp.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }
}

public class ServerMain {
    public static void main(String[] args) throws Exception {
        boolean[] inited = new boolean[4];
        Selector selector = null;
        int port = Integer.parseInt(args[0]);
        ServerSocketChannel sockerServer = null;
        while (true) {
            try {
                selector = Selector.open();
                sockerServer = ServerSocketChannel.open();
                sockerServer.bind(new InetSocketAddress("0.0.0.0", port));
                sockerServer.configureBlocking(false);
                sockerServer.register(selector, SelectionKey.OP_ACCEPT);
                ByteBuffer buffer = ByteBuffer.allocate(500000);

                int countPlayers = 0;

                MyServer server = null;

                while (true) {
                    selector.selectNow();
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if (key.isAcceptable()) {
                            SocketChannel client = sockerServer.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            client.keyFor(selector).attach(countPlayers);
                            ++countPlayers;
                            System.out.println("connection from: " + client.socket());
                            System.out.println("Now we have " + countPlayers + " players.");
                        }
                        iter.remove();
                    }
                    // TODO: just for test
                    if (countPlayers == 4) {
                        server = new MyServer();
                        System.out.println("server is ready!");
                        break;
                    }
                }

                while (true) {
                    Thread.sleep(50);
                    selector.selectNow();
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            int index = (Integer) key.attachment();
                            buffer.clear();
                            client.read(buffer);
                            buffer.flip();
                            while (buffer.remaining() >= 4) {
                                int KeyCode = buffer.getInt();
                                server.handleKeyCode(index, KeyCode);
                            }

                        }
                        if (key.isWritable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            int index = (Integer) key.attachment();

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            if (!inited[index]) {
                                inited[index] = true;
                                baos.write(index);
                            }
                            baos.write(server.getPackage(index));

                            client.write(ByteBuffer.wrap(baos.toByteArray()));
                            baos.close();
                        }
                    }
                }
            } catch (Exception e) {
                selector.selectNow();
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        baos.write(99);

                        try {
                            client.write(ByteBuffer.wrap(baos.toByteArray()));
                        } catch (Exception ee) {
                        }
                        baos.close();
                    }
                }

                selector.close();
                sockerServer.close();
                System.out.println(e);
            }
        }
    }
}