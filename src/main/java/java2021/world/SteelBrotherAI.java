package java2021.world;

import java.util.*;

public class SteelBrotherAI extends PlayerAI {

    public SteelBrotherAI(Creature creature, World world, List<String> messages) {
        super(creature, world, messages);
    }

    @Override
    public void run() {
        onSkill = true;
        int defence = player.defenseValue();
        player.setGlyph((char) 151);
        player.setDefense(Integer.MAX_VALUE);
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
        player.setDefense(defence);
        player.setGlyph((char) 148);
        onSkill = false;
    }
}