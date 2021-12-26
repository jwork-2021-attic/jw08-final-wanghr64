package java2021.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Player extends Creature {

    public int index;

    public Player(int index, World world, char glyph, Color color, int maxHP, int attack, int defense,
            int visionRadius) {
        super(world, glyph, color, maxHP, attack, defense, visionRadius);
        this.validAIs = new boolean[7];
        this.index = index;
        Arrays.fill(this.validAIs, true);
        this.validAIs[0] = true;
        maxCoolTime = new int[] { 0, 10, 10, 10, 10, 10, 10 };
        costCoolTime = new int[] { 0, 1, 10, 3, 7, 10, 10 };
        curCoolTime = new int[7];

        messages = new ArrayList<>();

        myAIs = new PlayerAI[7];
        myAIs[0] = new OldManAI(this, world);
        myAIs[1] = new PowerBrotherAI(this, world);
        myAIs[2] = new ViewBrotherAI(this, world);
        myAIs[3] = new FireBrotherAI(this, world);
        myAIs[4] = new WaterBrotherAI(this, world);
        myAIs[5] = new SteelBrotherAI(this, world);
        myAIs[6] = new HideBrotherAI(this, world);
    }

    public int digCount = 0;

    public boolean win = false;

    public boolean isVisible = true;

    public boolean[] validAIs;
    public int iCurAI;
    private PlayerAI[] myAIs;
    private List<String> messages;

    public int[] curCoolTime;
    public final int[] maxCoolTime;
    public final int[] costCoolTime;

    public PlayerAI curAI() {
        return myAIs[iCurAI];
    }

    public boolean onSkill() {
        return curAI().onSkill();
    }

    public boolean freeze() {
        return curAI().freeze();
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
        new Thread(curAI()).start();
    }
}
