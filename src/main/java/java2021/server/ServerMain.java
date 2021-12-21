package java2021.server;

import java.nio.*;
import java.nio.channels.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Predicate;

import java2021.world.*;
import java2021.MyUtils;
import java2021.asciiPanel.*;

class MyServer extends Thread {

    private World world;
    private Player[] players;
    private PlayerAI[][] pAIs;
    private int[] curAIs;
    private int[] preDirects;
    private int[] HPs;
    private List<String> messages;
    private Random rd;

    MyServer() {
        players = new Player[4];
        pAIs = new PlayerAI[4][7];
        curAIs = new int[4];
        preDirects = new int[4];
        HPs = new int[4];
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
            curAIs[i] = 0;
            players[i].setAI(pAIs[i][curAIs[i]]);
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

}

public class ServerMain {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        int port = Integer.parseInt(args[0]);
        ServerSocketChannel sockerServer = ServerSocketChannel.open();
        sockerServer.bind(new InetSocketAddress("0.0.0.0", port));
        sockerServer.configureBlocking(false);
        sockerServer.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(8192);

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
            if (countPlayers != 0) {
                server = new MyServer();
                break;
            }
        }

        while (true) {
            Thread.sleep(100);
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
                        // TODO: parse KeyCode
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
                    System.out.println("WRITE, to " + client.socket());
                    baos.close();
                    tempBaos.close();
                }
            }
        }
    }
}