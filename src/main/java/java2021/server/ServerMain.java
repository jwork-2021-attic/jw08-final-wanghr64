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

class MyServer extends Thread {

    private World world;
    private Player[] players;
    private PlayerAI[][] pAIs;
    private List<String> messages;
    private Random rd;

    MyServer() {
        players = new Player[4];
        pAIs = new PlayerAI[4][7];
        rd = new Random();
        createWorld();
        createPlayers();
        createEnemies();
        createBonuses();
    }

    private void createWorld() {
        world = new WorldBuilder(90, 60).makeCaves().build();
    }

    private void createPlayers() {
        for (int i = 0; i < 4; ++i) {
            players[i] = new Player(this.world, (char) 138, AsciiPanel.fromPic, 100, 20, 10, 9);
            world.addAtEmptyLocation(players[i]);
            pAIs[i][0] = new OldManAI(players[i], world, messages);
            pAIs[i][1] = new PowerBrotherAI(players[i], world, messages);
            pAIs[i][2] = new ViewBrotherAI(players[i], world, messages);
            pAIs[i][3] = new FireBrotherAI(players[i], world, messages);
            pAIs[i][4] = new WaterBrotherAI(players[i], world, messages);
            pAIs[i][5] = new SteelBrotherAI(players[i], world, messages);
            pAIs[i][6] = new HideBrotherAI(players[i], world, messages);
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
    }

    public Package2Client getPackage(int index) {
        if (world == null || players[index] == null)
            System.out.println("skljdgufdfdg");
        return new Package2Client(world, players[index]);
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
}

public class ServerMain {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        int port = Integer.parseInt(args[0]);
        ServerSocketChannel sockerServer = ServerSocketChannel.open();
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
            Thread.sleep(200);
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
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(server.getPackage(index));
                    oos.close();
                    ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();
                    MyUtils.addInt2ByteArrayOS(tempBaos, baos.size());
                    tempBaos.writeBytes(baos.toByteArray());
                    client.write(ByteBuffer.wrap(tempBaos.toByteArray()));
                    baos.close();
                    tempBaos.close();
                }
            }
        }
    }
}