package team008.donothing;

import battlecode.common.*;

public class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {

        TreeInfo[] nearby = rc.senseNearbyTrees();
        if (nearby.length > 0){
            int start = Clock.getBytecodeNum();
            for (int i = nearby.length; i-- > 0;) {
                float dist = rc.getLocation().distanceTo(nearby[i].getLocation());
                dist = rc.getLocation().distanceTo(nearby[i].getLocation());
                dist = rc.getLocation().distanceTo(nearby[i].getLocation());
                dist = rc.getLocation().distanceTo(nearby[i].getLocation());
            }
            System.out.println("method 1: " + (Clock.getBytecodeNum() - start));

            start = Clock.getBytecodeNum();
            for (int i = 0; i < nearby.length; i++) {
                float dist = rc.getLocation().distanceTo(nearby[i].getLocation());
                dist = rc.getLocation().distanceTo(nearby[i].getLocation());
                dist = rc.getLocation().distanceTo(nearby[i].getLocation());
                dist = rc.getLocation().distanceTo(nearby[i].getLocation());
            }
            System.out.println("method 3: " + (Clock.getBytecodeNum() - start));

            start = Clock.getBytecodeNum();
            for(TreeInfo t : nearby) {
                float dist = rc.getLocation().distanceTo(t.getLocation());
                dist = rc.getLocation().distanceTo(t.getLocation());
                dist = rc.getLocation().distanceTo(t.getLocation());
                dist = rc.getLocation().distanceTo(t.getLocation());
            }
            System.out.println("method 2: " + (Clock.getBytecodeNum() - start));
        }
        /********** FINDINGS :
         * method 1 is better with only 1-2 accessions (using nearby[i] / t)
         *
         * method 2 is better with 3 accessions or more
         *
         * method 3 is always worse than 1
         */

        while (true) {
            while(rc.getTeamBullets() >= rc.getVictoryPointCost()){
                rc.donate(rc.getVictoryPointCost());
            }
            Clock.yield();
        }
    }
}