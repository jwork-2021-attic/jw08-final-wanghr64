package java2021.world;

import java.io.Serializable;

public class Bonus implements Serializable {

    public Bonus(World world, int type) {
        this.world = world;
        this.type = type;
    }

    private int type;

    public int type() {
        return type;
    }

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

    public char glyph() {
        switch (this.type) {
            case 0:
                return (char) 133;
            case 1:
                return (char) 141;
            case 2:
                return (char) 142;
            case 999:
                return (char) 165;
            default:
                return (char) 132;
        }
    }

    public void remove() {
        world.removeBonus(this);
    }
}
