package java2021.world;

import java.util.*;

public class ViewBrotherAI extends PlayerAI {

    public ViewBrotherAI(Creature creature, World world) {
        super(creature, world);
    }

    @Override
    public void run() {
        onSkill = true;
        allSee = true;
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println(e);
        }
        allSee = false;
        onSkill = false;
    }
}