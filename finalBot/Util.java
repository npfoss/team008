package team008.finalBot;

import battlecode.common.*;

public class Util extends Bot {

    ////////////------ DISTANCE methods-----/////////////

    public static float distanceSquaredTo(MapLocation loc1, MapLocation loc) {
        return ((loc.x - loc1.x) * (loc.x - loc1.x)) + ((loc.y - loc1.y) * (loc.y - loc1.y));
    }

    public static BodyInfo closestBody(BodyInfo[] robots, MapLocation toHere) {
        BodyInfo closest = null;
        float bestDist = 999999;
        float dist;
        for (int i = robots.length; i-- > 0; ) {
            dist = distanceSquaredTo(toHere, robots[i].getLocation());
            if (dist < bestDist) {
                bestDist = dist;
                closest = robots[i];
            }
        }
        return closest;
    }

    ////////////------ leastHealth stuff ----/////////////

    public static RobotInfo leastHealth(RobotInfo[] robots, boolean excludeArchons) {
        RobotInfo ret = null;
        double minHealth = 99999;
        for (RobotInfo bot : robots) {
            if (bot.health < minHealth && (!excludeArchons || bot.type != RobotType.ARCHON)) {
                minHealth = bot.health;
                ret = bot;
            }
        }
        return ret;
    }

    public static TreeInfo leastHealth(TreeInfo[] trees, boolean canWater) {
        TreeInfo ret = null;
        double minHealth = 1e99;
        for (TreeInfo tree : trees) {
            if ((!canWater || rc.canWater(tree.ID)) && tree.health < minHealth) {
                minHealth = tree.health;
                ret = tree;
            }
        }
        return ret;
    }

    public static TreeInfo leastHealth(TreeInfo[] trees) {
        TreeInfo ret = null;
        double minHealth = 1e99;
        for (TreeInfo tree : trees) {
            if (tree.health < minHealth) {
                minHealth = tree.health;
                ret = tree;
            }
        }
        return ret;
    }

    ////////////------ misc/unsorted --------/////////////
    
    public static RobotInfo closestSpecificType(RobotInfo[] robots, MapLocation toHere, RobotType type) {
        RobotInfo closest = null;
        for (int i = robots.length; i-- > 0;) {
            if (robots[i].type == type) {
            	closest = robots[i];
                break;
            }
        }
        return closest;
    }
    
    public static Direction randomDirection() {
        return new Direction(myRand.nextFloat() * 2 * (float) Math.PI);
    }

    public static boolean isDangerous(RobotType t) {
        return !(t == RobotType.ARCHON || t == RobotType.GARDENER);
    }


    public static TreeInfo[] combineTwoTIArrays(TreeInfo[] array1, TreeInfo[] array2) {
        TreeInfo[] combo = new TreeInfo[array1.length + array2.length];
        for (int i = 0; i < array1.length; i++) {
            combo[i] = array1[i];
        }
        for (int i = 0; i < array2.length; i++) {
            combo[i + array1.length] = array2[i];
        }
        return combo;
    }

    public static MapLocation centroidOfUnits(RobotInfo[] robots) {
        float xavg = 0, yavg = 0;
        MapLocation loc;
        for (RobotInfo bot : robots) {
            loc = bot.location;
            xavg += loc.x;
            yavg += loc.y;
        }
        return new MapLocation(xavg / robots.length, yavg / robots.length);
    }
}

