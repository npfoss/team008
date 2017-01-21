package team008.soldierBot00;

import java.util.Random;
import battlecode.common.*;

public class Bot {
	//for debugging
	public boolean debug = false;

	//for everyone to use
    public static RobotController rc;
    public static RobotType type;
    public static Team enemy;
    public static Team us;
    public static MapLocation here;
    

    //finding these is expensive so lets only do it once
    public static TreeInfo[] nearbyTrees;
    public static TreeInfo[] nearbyNeutralTrees;
    public static TreeInfo[] nearbyAlliedTrees;
    public static TreeInfo[] nearbyEnemyTrees;
    public static RobotInfo[] nearbyRobots;
    public static RobotInfo[] nearbyAlliedRobots;
    public static RobotInfo[] nearbyEnemyRobots;
    public static BulletInfo[] nearbyBullets;
    public static Random myRand;
    public static int strategy = 0;
    public static int roundNum;
    public static boolean isLeader = false;
    public static boolean isDead = false;
    public Bot(){}

	public Bot(RobotController r) throws GameActionException {
		rc = r;
		type = rc.getType();
		enemy = rc.getTeam().opponent();
		us = rc.getTeam();
		here = rc.getLocation();
		myRand = new Random(rc.getID());
		MapAnalysis.center = MapAnalysis.findCenter();
	}

	public void loop() {
		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				here = rc.getLocation();
                nearbyTrees = rc.senseNearbyTrees(-1);
                FastMethods.initializeNearbyNeutralTrees();
                FastMethods.initializeNearbyEnemyTrees();
                FastMethods.initializeNearbyAlliedTrees();
				nearbyRobots = rc.senseNearbyRobots(-1);
				FastMethods.initializeNearbyAlliedRobots();
				FastMethods.initializeNearbyEnemyRobots();
                nearbyBullets = rc.senseNearbyBullets();
                roundNum = rc.getRoundNum();
                if(rc.readBroadcast(10) == 0 && rc.getHealth() > 20){
                	isLeader = true;
            		rc.broadcast(10, 1);
                }
                if(isLeader){
                	MapAnalysis.makeDecisions();
                }
                if(!isDead && rc.getHealth() < 20){
                	if (isLeader){
                		isLeader = false;
                		rc.broadcast(10, 0);
                	}
                	switch(type){
                	case ARCHON:
                		rc.broadcast(4,rc.readBroadcast(4)-1);
                		break;
                	case GARDENER:
                		rc.broadcast(5,rc.readBroadcast(5)-1);
                		break;
                	case SOLDIER:
                		rc.broadcast(6,rc.readBroadcast(6)-1);
                		break;
                	case TANK:
                		rc.broadcast(7,rc.readBroadcast(7)-1);
                		break;
                	case SCOUT:
                		rc.broadcast(8,rc.readBroadcast(8)-1);
                		break;
                	case LUMBERJACK:
                		rc.broadcast(9,rc.readBroadcast(9)-1);
                		break;
					default:
						break;
					
                	}
                	isDead = true;
                }
               //test this
                if (roundNum == GameConstants.GAME_DEFAULT_ROUNDS
						|| rc.getTeamVictoryPoints() + rc.getTeamBullets() / 10 >= 1000) {
					rc.donate(((int) (rc.getTeamBullets() / 10)) * 10);
				}
				shakeNearbyTrees();
				takeTurn();
				if (rc.canShake()) {
					shakeNearbyTrees();
				}
			} catch (Exception e) {
				System.out.println(rc.getType().toString() + " Exception :(");
				e.printStackTrace();
			}
			// Clock.yield() makes the robot wait until the next turn, then it
			// will perform this loop again
			Clock.yield();
		}
	}

	public void takeTurn() throws Exception {
		return;
	}

	public void shakeNearbyTrees() throws Exception {
		for (TreeInfo tree : nearbyNeutralTrees) {
			if (tree.containedBullets > 0 && rc.canShake(tree.ID)) {
				rc.shake(tree.ID);
				return;
			}
		}
	}


	/******* ALL NAVIGATION METHODS BELOW *******/
	// TODO: navigate/implement bugging
	private static MapLocation dest = null;
	private static boolean isBugging = false;
	public static Direction calculatedMove;

	protected static int dangerRating(MapLocation loc) {
		float danger = 0;
		for (BulletInfo b : nearbyBullets) {
			if (willCollide(b, loc)) {
				danger += 10;
			}
		}
		boolean enemiesNearby = nearbyEnemyRobots.length > 0;
		for (RobotInfo l : nearbyRobots) {
			if (l.team == enemy && type != RobotType.LUMBERJACK && l.type == RobotType.LUMBERJACK) {
				if (loc.distanceTo(l.location) < 3.51 + type.bodyRadius) {
					danger += (10.0 - loc.distanceTo(l.location));
				}
			} else if (l.team == us && enemiesNearby) {
				if (l.type == RobotType.LUMBERJACK) {
					if (loc.distanceTo(l.location) < 2.1 + type.bodyRadius) {
						danger += (10.0 - loc.distanceTo(l.location)) ;
					}
				}

			}
		}
		return (int) (danger);
	}

	private static int tryMove(Direction dir, float dist, boolean makeMove) throws GameActionException {
		if (rc.canMove(dir, dist)) {
			int danger = 0;
			danger = dangerRating(here.add(dir, dist));
			if (danger == 0) {
				if (makeMove) {
					rc.move(dir, dist);
					here = rc.getLocation();
				}
			}
			calculatedMove = dir;
			return danger;
		}
		return 9999;

	}

	public static boolean tryMoveDirection(Direction dir, boolean makeMove, boolean goBackwards) throws GameActionException {
		Direction bestDir = dir;
		int bestDanger = 9999;
		int tempDanger = 0;
		tempDanger = tryMove(dir, type.strideRadius, makeMove);
		if (tempDanger == 0) {
			return true;
		}
		if (tempDanger < bestDanger) {
			bestDir = dir;
			bestDanger = tempDanger;
		}
		Direction left = dir.rotateLeftDegrees(30);
		Direction right = dir.rotateRightDegrees(30);
		for (int i = 0; i < (goBackwards ? 6:3); i++) {

			tempDanger = tryMove(left, type.strideRadius, makeMove);
			if (tempDanger == 0) {
				return true;
			}
			if (tempDanger < bestDanger) {
				bestDir = left;
				bestDanger = tempDanger;
			}
			tempDanger = tryMove(right, type.strideRadius, makeMove);
			if (tempDanger == 0) {
				return true;
			}
			if (tempDanger < bestDanger) {
				bestDir = right;
				bestDanger = tempDanger;
			}
			left = left.rotateLeftDegrees(30);
			right = right.rotateRightDegrees(30);
		}
		tempDanger = dangerRating(here);
		if (tempDanger < bestDanger) {
			bestDir = null;
			bestDanger = tempDanger;
		}
		calculatedMove = bestDir;
		if (bestDir != null && makeMove) {
			rc.move(bestDir, type.strideRadius);
			here = rc.getLocation();
			return true;
		}
		return false;
	}

	public static void goTo(Direction dir) throws GameActionException {
		tryMoveDirection(dir, true, true);
	}

	public static void goTo(MapLocation theDest) throws GameActionException {
		// for now
		if (theDest == null) {
			tryMoveDirection(here.directionTo(MapAnalysis.center), true, true);
		}
		if (dest != null && dest.distanceTo(theDest) < .001) {
			// continue bugging
		} else {
			// no more bugging
			dest = theDest;
			isBugging = false;
		}

		if (!isBugging || 1 == 1) {
			
			if (tryMoveDirection(here.directionTo(dest), true, true)) {
				return;
			} else {
				isBugging = true;
				// for now we give up
				return;
			}
		}
	}

//	public static void explore() throws GameActionException {
//		if (dirIAmMoving == null) {
//			dirIAmMoving = here.directionTo(MapAnalysis.center);
//		}
//		if (nearbyAlliedRobots != null) {
//			for (RobotInfo r : nearbyAlliedRobots) {
//				if (r.type == RobotType.SCOUT && dirIAmMoving.degreesBetween(here.directionTo(r.location)) < 45) {
//					dirIAmMoving = dirIAmMoving.opposite();
//					break;
//				}
//			}
//		}
//		if (myRand.nextFloat() < 0.1) {
//			// System.out.println(dirIAmMoving);
//			dirIAmMoving = dirIAmMoving.rotateLeftDegrees(100);
//		}
//		goTo(dirIAmMoving);
//	}

	/////////////////////////////// Dangerous Nav///////////////////////////////
	public static void goToDangerous(MapLocation loc) throws GameActionException{
		if (here.distanceTo(loc) < type.strideRadius
				&& rc.canMove(loc)) {
			rc.move(loc);
			here = rc.getLocation();
			return;
		}
		tryMoveDirectionDangerous(here.directionTo(loc));
	}
	public static boolean tryMoveDirectionDangerous(Direction dir) throws GameActionException {
		if (tryMoveDangerous(dir, type.strideRadius)) {
			return true;
		}
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for (int i = 0; i < 17; i++) {
			if (tryMoveDangerous(left, type.strideRadius)) {
				return true;
			}
			if (tryMoveDangerous(right, type.strideRadius)) {
				return true;
			}
			left = left.rotateLeftDegrees(10);
			right = right.rotateRightDegrees(10);
		}
		return false;
	}
	private static boolean tryMoveDangerous(Direction dir, float dist) throws GameActionException {
		if (rc.canMove(dir, dist)) {
			rc.move(dir, dist);
			here = rc.getLocation();
			return true;
		} else {
			return false;
		}
	}

	public static boolean willCollide(BulletInfo bullet, MapLocation loc) {
		Direction directionToRobot = bullet.location.directionTo(loc);
		float distToRobot = bullet.location.distanceTo(loc);
		float theta = Math.abs(bullet.dir.radiansBetween(directionToRobot));
		if (theta > Math.PI / 2) {
			return false;
		}
		float perpendicularDist = (float) (distToRobot * Math.sin(theta)); 
		return (perpendicularDist < type.bodyRadius+.01 && (distToRobot - type.bodyRadius < bullet.speed+.01));
	}
}
