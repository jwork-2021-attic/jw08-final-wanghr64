package java2021.server;

import java.io.*;
import java.util.*;
import java2021.world.*;

public class Package2Client implements Serializable {
    private World world;
    private Player player;

    public Package2Client(World world, Player player) {
        this.world = world;
        this.player = player;
    }

    public World world() {
        return this.world;
    }

    public Player player() {
        return this.player;
    }
}
