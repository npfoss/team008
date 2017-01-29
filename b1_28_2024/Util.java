package team008.b1_28_2024;

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

    public static TreeInfo closestTree(TreeInfo[] trees, MapLocation toHere, boolean excludeEmpty) {
        return closestTree(trees, toHere, excludeEmpty, 9999, false);
    }

    public static TreeInfo closestTree(TreeInfo[] trees, MapLocation toHere, boolean excludeEmpty, int whenToGiveUp) {
        return closestTree(trees, toHere, excludeEmpty, whenToGiveUp, false);
    }

    public static TreeInfo closestTree(TreeInfo[] trees, MapLocation toHere, boolean excludeEmpty, int whenToGiveUp, boolean careAboutHealth){
        TreeInfo closest = null;
        float bestDist = 999999;
        float dist;
        float bestHealth = 999999;
        for (int i = Math.min(whenToGiveUp, trees.length); i-- > 0; ) {
            if (!excludeEmpty || trees[i].containedRobot != null) {
                dist = here.distanceTo(trees[i].getLocation());
                if (careAboutHealth && dist < GameConstants.LUMBERJACK_STRIKE_RADIUS + trees[i].getRadius()){
                    if( trees[i].getHealth() < bestHealth){
                        bestHealth = trees[i].getHealth();
                        closest = trees[i];
                        bestDist = dist;
                    }
                } else if (dist < bestDist) {
                    bestDist = dist;
                    closest = trees[i];
                }
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

    public static TreeInfo leastHealthTouchingRadius(TreeInfo[] trees, MapLocation toHere, float radius, boolean withContainedUnit) {
        TreeInfo ret = null;
        double minHealth = 999999;
        for (TreeInfo tree : trees){
            if (tree.health < minHealth && (!withContainedUnit || tree.containedRobot != null) && distanceSquaredTo(toHere, tree.getLocation()) < (radius + tree.getRadius())*(radius + tree.getRadius())) {
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

    public static int numBodiesTouchingRadius(BodyInfo[] trees, MapLocation toHere, float radius) {
        return numBodiesTouchingRadius(trees, toHere, radius, 9999);
    }

    public static int numBodiesTouchingRadius(BodyInfo[] trees, MapLocation toHere, float radius, int whenToGiveUp){
        int count = 0;
        for (int i = Math.min(whenToGiveUp, trees.length); i-->0;){
            if (toHere.distanceTo(trees[i].getLocation()) <= radius + trees[i].getRadius()){
                count++;
            }
        }
        return count;
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

	public static MapLocation midpoint(MapLocation a, MapLocation b) {
		return new MapLocation((a.x + b.x)/2, (a.y + b.y)/2);
	}
}

