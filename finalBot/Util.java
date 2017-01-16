package team008.finalBot;

import battlecode.common.*;

public class Util extends Bot {

    ////////////------ DISTANCE methods-----/////////////

    public static float distanceSquaredTo(MapLocation loc1, MapLocation loc){
        return ((loc.x-loc1.x)*(loc.x - loc1.x)) + ((loc.y-loc1.y)*(loc.y - loc1.y));
    }

    public static MapLocation closestLocation(MapLocation[] locs, MapLocation toHere) {
        float bestDist = 999999;
        float dist;
        MapLocation bestLoc = null;
        for (MapLocation loc : locs) {
            if(loc == null){ // does this ever actually happen?
                continue;
            }
            dist = distanceSquaredTo(toHere, loc);
            if (dist < bestDist) {
                bestDist = dist;
                bestLoc = loc;
            }
        }
        return bestLoc;
    }

    public static BodyInfo closestBody(BodyInfo[] robots, MapLocation toHere) {
        BodyInfo closest = null;
        float bestDist = 999999;
        float dist;
        for (int i = robots.length; i-- > 0;) {
            dist = distanceSquaredTo(toHere, robots[i].getLocation());
            if (dist < bestDist) {
                bestDist = dist;
                closest = robots[i];
            }
        }
        return closest;
    }

    public static float distToClosestBody(BodyInfo[] robots, MapLocation toHere) {
        float bestDist = 999999;
        float dist;
        for (int i = robots.length; i-- > 0;) {
            dist = toHere.distanceTo(robots[i].getLocation());
            if (dist < bestDist) {
                bestDist = dist;
            }
        }
        return bestDist;
    }

    public static RobotInfo closestRobot(RobotInfo[] robots, MapLocation toHere) {
        return (RobotInfo)closestBody(robots, toHere);
    }

    public static TreeInfo closestTree(TreeInfo[] trees, MapLocation toHere) {
        return (TreeInfo)closestBody(trees, toHere);
    }

    public static TreeInfo closestTree(TreeInfo[] robots, MapLocation toHere, int size) {
        TreeInfo closest = null;
        float bestDist = 999999;
        float dist;
        for (int i = size; i-- > 0;) {
            dist = distanceSquaredTo(toHere, robots[i].location);
            if (dist < bestDist) {
                bestDist = dist;
                closest = robots[i];
            }
        }
        return closest;
    }

    public static BulletInfo closestBullet(BulletInfo[] bullets, MapLocation toHere) {
        return (BulletInfo)closestBody(bullets, toHere);
    }

    public static RobotInfo closestSpecificType(RobotInfo[] robots, MapLocation toHere, RobotType type) {
        RobotInfo closest = null;
        float bestDist = 99999;
        float dist;
        for (int i = robots.length; i-- > 0;) {
            if (robots[i].type == type) {
                dist = distanceSquaredTo(toHere, robots[i].location);
                if (dist < bestDist) {
                    bestDist = dist;
                    closest = robots[i];
                }
            }
        }
        return closest;
    }

    public static RobotInfo closestSpecificTypeOnTeam(RobotInfo[] robots, MapLocation toHere, RobotType type, Team team) {
        RobotInfo closest = null;
        float bestDist = 99999;
        float dist;
        for (RobotInfo bot : robots) {
            if (bot.type == type && bot.getTeam() == team) {
                dist = distanceSquaredTo(toHere, bot.location);
                if (dist < bestDist) {
                    bestDist = dist;
                    closest = bot;
                }
            }
        }
        return closest;
    }

    public static RobotInfo closestRobotOnTeam(RobotInfo[] robots, MapLocation toHere,Team team) {
        RobotInfo closest = null;
        float bestDist = 99999;
        float dist;
        for (int i = robots.length; i-- > 0;) {
            if (robots[i].type == type) {
                dist = distanceSquaredTo(toHere, robots[i].location);
                if (dist < bestDist && robots[i].getTeam() == team) {
                    bestDist = dist;
                    closest = robots[i];
                }
            }
        }
        return closest;
    }

    public static float avgDistTo(BodyInfo[] robots, MapLocation loc){
        if (robots.length == 0){
            return -1;
        }
        float sum = 0;
        for (BodyInfo bot : robots){
            sum += loc.distanceTo(bot.getLocation());
        }
        return sum / robots.length;
    }

    ////////////------ counting methods -----/////////////
    public static int numHostileUnits(RobotInfo[] enemies){
        int ret = 0;
        for(RobotInfo e: enemies){
            if(e.team == enemy && e.type != RobotType.ARCHON && e.type != RobotType.GARDENER){
                ret++;
            }
        }
        return ret;
    }

    public static int numBodiesTouchingRadius(BodyInfo[] trees, MapLocation toHere, float radius){
        int count = 0;
        for (BodyInfo tree : trees){
            if (distanceSquaredTo(toHere, tree.getLocation()) <= (radius + tree.getRadius())*(radius + tree.getRadius())){
                count++;
            } 
        }
        return count;
    }

    ////////////------ leastHealth stuff ----/////////////

    public static RobotInfo leastHealth(RobotInfo[] robots, boolean excludeArchons) {
        RobotInfo ret = null;
        double minHealth = 99999;
        for (RobotInfo bot : robots) {
            if (bot.health < minHealth && ( !excludeArchons || bot.type != RobotType.ARCHON)) {
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
        for (TreeInfo tree : trees){
            if (tree.health < minHealth) {
                minHealth = tree.health;
                ret = tree;
            }
        }
        return ret;
    }

    public static TreeInfo leastHealthTouchingRadius(TreeInfo[] trees, MapLocation toHere, float radius) {
        TreeInfo ret = null;
        double minHealth = 999999;
        for (TreeInfo tree : trees){
            if (tree.health < minHealth && distanceSquaredTo(toHere, tree.getLocation()) < (radius + tree.getRadius())*(radius + tree.getRadius())) {
                minHealth = tree.health;
                ret = tree;
            }
        }
        return ret;
    }

    ////////////------ misc/unsorted --------/////////////
    public static Direction randomDirection() {
        return new Direction( myRand.nextFloat() * 2 * (float) Math.PI);
    }
    public static boolean isDangerous(RobotType t){
    	return !(t == RobotType.ARCHON || t == RobotType.GARDENER);
    }

    public static float radians(int degrees){ return degrees * (float)Math.PI / 180; }

    public static boolean containsBodiesTouchingRadius(BodyInfo[] robots, MapLocation toHere, float radius){
        for (BodyInfo bot : robots){
            if (distanceSquaredTo(toHere, bot.getLocation()) <= (radius + bot.getRadius())*(radius + bot.getRadius())){
                return true;
            }
        }
        return false;
    }
    
    public static TreeInfo[] combineTwoTIArrays( TreeInfo[] array1, TreeInfo[] array2){
        TreeInfo[] combo = new TreeInfo[array1.length + array2.length];
        for (int i = 0; i < array1.length; i++){
            combo[i] = array1[i];
        }
        for (int i = 0; i < array2.length; i++){
            combo[i + array1.length] = array2[i];
        }
        return combo;
    }

    public static MapLocation centroidOfUnits(RobotInfo[] robots){
        float xavg = 0, yavg = 0;
        MapLocation loc;
        for(RobotInfo bot : robots){
            loc = bot.location;
            xavg += loc.x;
            yavg += loc.y;
        }
        return new MapLocation(xavg/robots.length,yavg/robots.length);
    }

    ///--------- UNUSED METHODS FROM LAST YEAR--------///

    public static BodyInfo[] combineTwoRIArrays( BodyInfo[] array1, BodyInfo[] array2){
        BodyInfo[] combo = new BodyInfo[array1.length + array2.length];
        for (int i = 0; i < array1.length; i++){
            combo[i] = array1[i];
        }
        for (int i = 0; i < array2.length; i++){
            combo[i + array1.length] = array2[i];
        }
        return combo;
    }

    public static RobotInfo[] combineTwoRIArrays( RobotInfo[] array1, RobotInfo[] array2) {
        RobotInfo[] combo = new RobotInfo[array1.length + array2.length];
        for (int i = 0; i < array1.length; i++) {
            combo[i] = array1[i];
        }
        for (int i = 0; i < array2.length; i++) {
            combo[i + array1.length] = array2[i];
        }
        return combo;
    }

    public static boolean containsMapLocation(MapLocation[] locs, MapLocation location, int size) {
        for(int i = 0; i < size; i++){
            if(locs[i] == null){
                continue;
            }
            if(locs[i].equals(location)){
                return true;
            }
        }
        return false;
    }

    public static void removeIndexFromArray(Object[] array, int index, int size){
        for(int i = index; i < size - 1; i++){
            array[i] = array[i+1];
        }
    }

    public static MapLocation getLocationOfType(RobotInfo[] array, RobotType t){
        for(RobotInfo ri : array)
            if(ri.type == t)
                return ri.location;
        return null;
    }

    public static RobotInfo[] getUnitsOfType(RobotInfo[] array, RobotType t) {
        int[] inds = new int[array.length];
        int s = 0;
        for(int i = array.length; i --> 0; ){
            if(array[i].type == t){
                inds[s++] = i;
            }
        }
        RobotInfo[] units = new RobotInfo[s];
        for(int i = s ; i --> 0 ; ){
            units[i] = array[inds[i]];
        }
        return units;
    }

    public static BodyInfo[] combineThreeRIArrays( BodyInfo[] array1, BodyInfo[] array2, BodyInfo[] array3){
    	BodyInfo[] combo = new RobotInfo[array1.length + array2.length + array3.length];
        for (int i = 0; i < array1.length; i++){
            combo[i] = array1[i];
        }
        for (int i = 0; i < array2.length; i++){
            combo[i + array1.length] = array2[i];
        }
        for (int i = 0; i < array3.length; i++){
            combo[i + array1.length + array2.length] = array3[i];
        }
        return combo;
    }

    public static int indexOfLocation(MapLocation[] array, MapLocation loc){
        for(int i = array.length; i --> 0; )
            if(array[i] != null && array[i].equals(loc))
                return i;
        return -1;
    }
}