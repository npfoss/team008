package team008.finalBot;

import battlecode.common.*;

public class Bot {
    public static RobotController rc;
    public static RobotType type;
    public static Team enemy;
    public static MapLocation here;
    public Bot(){}

    public Bot(RobotController r){
        rc = r;
        type = rc.getType();
        enemy = rc.getTeam().opponent();
        here = rc.getLocation();
    }

    public void loop(){

    	//System.out.println("new bot initialized: " + rc.getType().toString());

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	here = rc.getLocation();
                takeTurn();
            } catch (Exception e) {
                System.out.println(rc.getType().toString() + " Exception :(");
                e.printStackTrace();
            }
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();
        }
    }

    public void takeTurn() throws Exception
    {
    	return;
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
		RobotInfo[] lumberjacks = rc.senseNearbyRobots();
		int danger = 0;
		for(BulletInfo b : bullets){
			if (willCollide(b,loc)){
				danger++;
			}
		}
		for (RobotInfo l : lumberjacks)
			if(l.type == RobotType.LUMBERJACK && loc.distanceTo(l.location) < RobotType.LUMBERJACK.bodyRadius + RobotType.LUMBERJACK.strideRadius*2){
				danger++;
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
		return false;
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
