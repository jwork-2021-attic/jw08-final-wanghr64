package java2021.world;

import java.awt.Color;
import java.util.Arrays;

public class Player extends Creature {
    public Player(World world, char glyph, Color color, int maxHP, int attack, int defense, int visionRadius) {
        super(world, glyph, color, maxHP, attack, defense, visionRadius);
        this.validAIs = new boolean[7];
        Arrays.fill(this.validAIs, false);
        this.validAIs[0] = true;
        maxCoolTime = new int[] { 0, 10, 10, 10, 10, 10, 10 };
        costCoolTime = new int[] { 0, 1, 10, 3, 7, 10, 10 };
        curCoolTime = new int[7];
    }

    public int digCount = 0;

    public boolean win = false;

    public boolean isVisible = true;

    public boolean[] validAIs;

    public int[] curCoolTime;
    public final int[] maxCoolTime;
    public final int[] costCoolTime;

    public boolean onSkill() {
        return ai.onSkill();
    }

    public boolean freeze() {
        return ai.freeze();
    }

    public void setDefense(int d) {
        this.defenseValue = d;
    }

    public static Color id2Color(int id) {
        switch (id) {
            case 0:
                return Color.LIGHT_GRAY;
            case 1:
                return Color.ORANGE;
            case 2:
                return Color.YELLOW;
            case 3:
                return Color.RED;
            case 4:
                return Color.BLUE;
            case 5:
                return Color.GREEN;
            case 6:
                return Color.CYAN;
            default:
                return Color.LIGHT_GRAY;
        }
    }

    public void skill() {
        new Thread(ai).start();
    }
}
