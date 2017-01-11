package team008.finalBot;

import battlecode.common.*;

public class Bot {
    public static RobotController rc;
    public static RobotType type;
    public static Team enemy;
    public static Team us;
    public static MapLocation here;
    public static Direction dirIAmMoving;
    public Bot(){}

    public Bot(RobotController r){
        rc = r;
        type = rc.getType();
        enemy = rc.getTeam().opponent();
        us = rc.getTeam();
        here = rc.getLocation();
        dirIAmMoving = Util.randomDirection();
    }

    public void loop(){

    	//System.out.println("new bot initialized: " + rc.getType().toString());

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	here = rc.getLocation();
            	TreeInfo[] nearbyNeutralTrees = shakeNearbyTrees();
                takeTurn(nearbyNeutralTrees);
            	if( rc. canShake()){
            	    shakeNearbyTrees();
                }
            } catch (Exception e) {
                System.out.println(rc.getType().toString() + " Exception :(");
                e.printStackTrace();
            }
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();
        }
    }

    public void takeTurn(TreeInfo[] nearbyNeutralTrees) throws Exception
    {
    	return;
    }

	public TreeInfo[] shakeNearbyTrees() throws Exception{
		TreeInfo[] nearbyNeutralTrees = rc.senseNearbyTrees((float)99, Team.NEUTRAL);
		TreeInfo shakeMe = Util.highestShakeableBulletTree(nearbyNeutralTrees);
		if (shakeMe != null){
			rc.shake(shakeMe.getID());
			if (rc.getType() != RobotType.SCOUT){
			    System.out.println("***A robot that isn't a scout just shook a tree!!!");
            }
		}
		return nearbyNeutralTrees;
	}


    /******* ALL NAVIGATION METHODS BELOW *******/
    // TODO: navigate
	private static MapLocation dest = null;
	
//	private enum BugState {
//		DIRECT, BUG
//	}
//
//	public enum WallSide {
//		LEFT, RIGHT
//	}
	
	private static boolean isBugging = false;
	private static int dangerRating(MapLocation loc){
		BulletInfo[] bullets = rc.senseNearbyBullets();
		RobotInfo[] lumberjacks = rc.senseNearbyRobots(-1, enemy);
		int danger = 0;
		for(BulletInfo b : bullets){
			if (willCollide(b,loc)){
				danger++;
			}
		}
		for (RobotInfo l : lumberjacks)
			if(l.type == RobotType.LUMBERJACK && loc.distanceTo(l.location) < RobotType.LUMBERJACK.bodyRadius + RobotType.LUMBERJACK.strideRadius*2){
				danger++;
				danger+= (5-loc.distanceTo(l.location));
			}
		return danger;
	}
	private static boolean tryMove(Direction dir, float dist) throws GameActionException{
		if (rc.canMove(dir, dist) && dangerRating(here.add(dir, dist))== 0){
			rc.move(dir,dist);
			return true;
		}
		else{
			return false;
		}
	}
	public static boolean tryMoveDirection(Direction dir) throws GameActionException{
		
		if(tryMove(dir,type.strideRadius)){
			return true;
		}
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for (int i =0; i < 17; i++){
		if(tryMove(left,type.strideRadius)){
			return true;
		}
		if(tryMove(right,type.strideRadius)){
			return true;
		}
		left = left.rotateLeftDegrees(10);
		right = right.rotateRightDegrees(10);
		}
		if(dangerRating(here) > 0){
			//oh shiz we under attack
			minimizeDanger();
			return true;
		}
		return false;
	}
	private static int scoutDangerRating(MapLocation loc){
		BulletInfo[] bullets = rc.senseNearbyBullets();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1,enemy);
		int danger = 0;
		for(BulletInfo b : bullets){
			if (willCollide(b,loc)){
				danger+=10;
			}
		}
		for (RobotInfo l : enemies)
			if( l.type != RobotType.ARCHON && l.type != RobotType.GARDENER && loc.distanceTo(l.location) < l.type.bodyRadius + type.strideRadius + (type==RobotType.LUMBERJACK?0:type.strideRadius) + .5){
				danger+= 5-(loc.distanceTo(l.location));
				
			}
		return danger;
	}
	private static boolean scoutTryMove(Direction dir, float dist) throws GameActionException{
		if (rc.canMove(dir, dist) && scoutDangerRating(here.add(dir, dist))== 0){
			rc.move(dir,dist);
			return true;
		}
		else{
			return false;
		}
	}
	public static boolean scoutTryMoveDirection(Direction dir) throws GameActionException{
		
		if(scoutTryMove(dir,type.strideRadius)){
			return true;
		}
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for (int i =0; i < 17; i++){
		if(scoutTryMove(left,type.strideRadius)){
			return true;
		}
		if(scoutTryMove(right,type.strideRadius)){
			return true;
		}
		left = left.rotateLeftDegrees(10);
		right = right.rotateRightDegrees(10);
		}
		if(dangerRating(here) > 0 && rc.senseNearbyBullets().length > 0){
			//oh shiz we under attack
			minimizeDanger();
			return true;
		}
		
		return false;
	}
	public static void minimizeDanger() throws GameActionException{
		int[] dangers = new int[73];
		dangers[0] = dangerRating(here)+1;//as to check that it was changed
		Direction dir = new Direction(0);
		for (int i = 1; i < 37; i++){
			if(rc.canMove(dir,type.strideRadius)){
				dangers[i] = dangerRating(here.add(dir,type.strideRadius))+1;
			}
			dir = dir.rotateLeftDegrees(10);
			
		}
		dir = new Direction(0);
		for (int i = 17; i < 73; i++){
			if(rc.canMove(dir,type.strideRadius/2)){
				dangers[i] = dangerRating(here.add(dir,type.strideRadius/2))+1;
			}
			dir = dir.rotateLeftDegrees(10);
		}
		int minIndex = 0;
		int minDanger = 100;
		for(int i = 0; i < 73; i++){
			if(dangers[i] < minDanger && dangers[i] > 0){
				minDanger = dangers[i];
				minIndex = i;
			}
		}
		dir = new Direction(0);
		if (minIndex == 0){
			return;
		}
		else if (minIndex < 17){
			dir= dir.rotateLeftDegrees(10 * (minIndex-1));
			rc.move(dir, type.strideRadius);
		}
		else{
			dir= dir.rotateLeftDegrees(10 * (minIndex-17));
			rc.move(dir, type.strideRadius/2);
		}
	}
	public static void goTo(MapLocation theDest) throws GameActionException {
		//for now
		if (dest != null && dest.distanceTo(theDest) < .001){
			//continue bugging
		}
		else{
			//no more bugging
			dest = theDest;
			isBugging = false;
		}
		
		if(!isBugging || 1==1){
			if(tryMoveDirection(here.directionTo(dest))){
				return;
			}
			else{
				isBugging = true;
				//for now we give up
				return;
			}
		}
		
		
	}
	
	public static void explore() throws GameActionException{
		if(Math.random() < 0.1){
			//System.out.println(dirIAmMoving);
			dirIAmMoving = dirIAmMoving.rotateLeftDegrees(100);
		}
		scoutTryMoveDirection(dirIAmMoving);
	}

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,type.strideRadius);
    }

//    /**
//     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
//     *
//     * @param dir The intended direction of movement
//     * @param degreeOffset Spacing between checked directions (degrees)
//     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
//     * @return true if a move was performed
//     * @throws GameActionException
//     */
//    boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
//
//        // First, try intended direction
//        if (rc.canMove(dir)) {
//            rc.move(dir);
//            return true;
//        }
//
//        // Now try a bunch of similar angles
//        boolean moved = false;
//        int currentCheck = 1;
//
//        while(currentCheck<=checksPerSide) {
//            // Try the offset of the left side
//            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
//                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
//                return true;
//            }
//            // Try the offset on the right side
//            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
//                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
//                return true;
//            }
//            // No move performed, try slightly further
//            currentCheck++;
//        }
//
//        // A move never happened, so return false.
//        return false;
//    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */

    public static boolean willCollide(BulletInfo bullet, MapLocation loc) {
        // TODO: check if bullet will hit something else first
        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(loc);
        float distToRobot = bulletLocation.distanceTo(loc);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
}
