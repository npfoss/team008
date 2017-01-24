package team008.finalBot;

import java.util.Random;
import battlecode.common.*;

public class Bot {
	// for debugging
	public static boolean debug = false;

	// for everyone to use
	public static RobotController rc;
	public static RobotType type;
	public static Team enemy;
	public static Team us;
	public static MapLocation here;
	public static Direction calculatedMove;

	// Most units need this
	public static Direction dirIAmMoving;
	public static MapLocation target;

	// finding these is expensive so lets only do it once
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
	private static BugState bugState;
	private static WallSide bugWallSide = null;

	public Bot() {}

	public Bot(RobotController r) throws GameActionException {
		rc = r;
		type = rc.getType();
		enemy = rc.getTeam().opponent();
		us = rc.getTeam();
		here = rc.getLocation();
		dirIAmMoving = null;
		myRand = new Random(rc.getID());
		MapAnalysis.center = MapAnalysis.findCenter();
		bugState = BugState.DIRECT;
		nearbyAlliedRobots = rc.senseNearbyRobots(-1, us);
		switch (type) {
		case ARCHON:
			Message.NUM_ARCHONS.setValue(Message.NUM_ARCHONS.getValue() + 1);
			break;
		case GARDENER:
			if (!(nearbyAlliedRobots[0].type == RobotType.ARCHON)) {
				Message.NUM_GARDENERS.setValue(Message.NUM_GARDENERS.getValue() + 1);
			}
			break;
		case SOLDIER:
			if (!(nearbyAlliedRobots[0].type == RobotType.GARDENER)) {
				Message.NUM_SOLDIERS.setValue(Message.NUM_SOLDIERS.getValue() + 1);
			}
			break;
		case TANK:
			if (!(nearbyAlliedRobots[0].type == RobotType.GARDENER)) {
				Message.NUM_TANKS.setValue(Message.NUM_TANKS.getValue() + 1);
			}
			break;
		case SCOUT:
			if (!(nearbyAlliedRobots[0].type == RobotType.GARDENER)) {
				Message.NUM_SCOUTS.setValue(Message.NUM_SCOUTS.getValue() + 1);
			}
			break;
		case LUMBERJACK:
			if (!(nearbyAlliedRobots[0].type == RobotType.GARDENER)) {
				Message.NUM_LUMBERJACKS.setValue(Message.NUM_LUMBERJACKS.getValue() + 1);
			}
			break;
		default:
			break;

		}
	}

	private enum BugState {
		DIRECT, BUG
	}

	public enum WallSide {
		LEFT, RIGHT
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
				if (nearbyTrees.length < 15) {
					FastMethods.initializeAllTrees();
				} else {
					nearbyNeutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
					nearbyEnemyTrees = rc.senseNearbyTrees(-1, enemy);
					nearbyAlliedTrees = rc.senseNearbyTrees(-1, us);
				}

				nearbyRobots = rc.senseNearbyRobots(-1);
				if (nearbyRobots.length < 15) {
					FastMethods.initializeAllRobots();
				} else {
					nearbyEnemyRobots = rc.senseNearbyRobots(-1, enemy);
					nearbyAlliedRobots = rc.senseNearbyRobots(-1, us);
				}
				nearbyBullets = rc.senseNearbyBullets();
				roundNum = rc.getRoundNum();
				if (Message.DECISION_MAKER.getValue() == 0 && rc.getHealth() > 20) {
					isLeader = true;
					Message.DECISION_MAKER.setValue(1);
				}
				if (isLeader) {
					MapAnalysis.makeDecisions();
				}
				if (!isDead && rc.getHealth() < 9) {
					if (isLeader) {
						isLeader = false;
						Message.DECISION_MAKER.setValue(0);
					}
					switch (type) {
					case ARCHON:
						Message.NUM_ARCHONS.setValue(Message.NUM_ARCHONS.getValue() - 1);
						break;
					case GARDENER:
						Message.NUM_GARDENERS.setValue(Message.NUM_GARDENERS.getValue() - 1);
						break;
					case SOLDIER:
						Message.NUM_SOLDIERS.setValue(Message.NUM_SOLDIERS.getValue() - 1);
						break;
					case TANK:
						Message.NUM_TANKS.setValue(Message.NUM_TANKS.getValue() - 1);
						break;
					case SCOUT:
						Message.NUM_SCOUTS.setValue(Message.NUM_SCOUTS.getValue() - 1);
						break;
					case LUMBERJACK:
						Message.NUM_LUMBERJACKS.setValue(Message.NUM_LUMBERJACKS.getValue() - 1);
						break;
					default:
						break;

					}
					isDead = true;
				}
				// test this
				if (roundNum+1 == GameConstants.GAME_DEFAULT_ROUNDS
						|| rc.getTeamVictoryPoints() + rc.getTeamBullets() /rc.getVictoryPointCost() >= 1000) {
					rc.donate(((int) (rc.getTeamBullets() / rc.getVictoryPointCost())) * rc.getVictoryPointCost());
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

	/******** Messaging Notifications *********/
	public void assignNewTarget() throws GameActionException {
		target = null;
		MapLocation targetD = Message.DISTRESS_SIGNALS.getClosestLocation(here);
		if (targetD != null) {
			if (debug) {
				//System.out.println("got target D");
			}		
		}
		target = Message.ENEMY_ARMIES.getClosestLocation(here);
		if ((targetD != null && target == null)
				|| ((targetD != null && target != null) && here.distanceTo(targetD) < here.distanceTo(target))) {
			target = targetD;
			return;
		}
		if (target == null) {
			target = Message.ISOLATED_ENEMIES.getClosestLocation(here);
			if (target == null) {
				target = targetD;
			}
		}
	}

	public static void notifyFriendsOfEnemies(RobotInfo[] enemies) throws GameActionException {
		if (Util.closestSpecificType(nearbyAlliedRobots, here, RobotType.GARDENER) != null && Util.closestSpecificType(nearbyAlliedRobots, here, RobotType.GARDENER).location.distanceTo(enemies[0].location) < 7) {
			Message.DISTRESS_SIGNALS.addLocation(enemies[0].location);
		}
		if(Util.closestSpecificType(nearbyEnemyRobots, here, RobotType.ARCHON) != null){
			Message.ENEMY_ARCHONS.addLocation(Util.closestSpecificType(nearbyEnemyRobots, here, RobotType.ARCHON).location);
		}
		if(enemies.length == 1 && enemies[0].type != RobotType.SCOUT && enemies[0].type != RobotType.ARCHON){
			Message.ISOLATED_ENEMIES.addLocation(enemies[0].location);
		} else if (enemies.length > 1) {
			Message.ENEMY_ARMIES.addLocation(Util.centroidOfUnits(enemies));
		}
	}

	/******* ALL NAVIGATION METHODS BELOW *******/
	private static MapLocation dest = null;
	private static boolean isBugging = false;
	private static int bugRotationCount;

	private static float bugStartDistSq;

	private static int bugMovesSinceSeenObstacle;

	private static Direction bugLastMoveDir;

	private static Direction bugLookStartDir;

	private static int bugMovesSinceMadeProgress;

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
				if (loc.distanceTo(l.location) < 3.51 + type.bodyRadius + (type == RobotType.SCOUT?2:0)) {
					danger += (10.0 - loc.distanceTo(l.location));
				}
			} else if (l.team == us && enemiesNearby) {
				if (l.type == RobotType.LUMBERJACK) {
					if (loc.distanceTo(l.location) < 2.1 + type.bodyRadius) {
						danger += (10.0 - loc.distanceTo(l.location));
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
				calculatedMove = dir;
				if (makeMove) {
					rc.move(dir, dist);
					here = rc.getLocation();
				}
			}
			return danger;
		}
		return 9999;
	}

	public static boolean tryMoveDirection(Direction dir, boolean makeMove, boolean goBackwards)
			throws GameActionException {
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
		for (int i = 0; i < (goBackwards ? 6 : 3); i++) {

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
		if(bestDir != null){
			calculatedMove = bestDir;
		}
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
		if (rc.getMoveCount() == 1 || here.distanceTo(theDest) < .1) {
			bugState = BugState.DIRECT;
			return;
		}
		if (theDest == null) {
			tryMoveDirection(here.directionTo(MapAnalysis.center), true, true);
			return;
		}
		
		if(dest == null || !theDest.isWithinDistance(dest, (float) .1)){
			dest = theDest;
			bugState = BugState.DIRECT;
			bugMovesSinceMadeProgress = 0;
		}
		bugMove();
	}

	private static void bugMove() throws GameActionException {
		if (debug)
			//System.out.println("bugging");
		if (bugState == BugState.BUG) {
			if (canEndBug()) {
				bugState = BugState.DIRECT;
				bugMovesSinceMadeProgress = 0;
			}
		}
		if (bugState == BugState.DIRECT) {
			if (!tryMoveDirect()) {
				bugState = BugState.BUG;
				startBug();
			}
		}
		if (bugState == BugState.BUG) {
			bugTurn();
			bugMovesSinceMadeProgress++;
		}
	}

	private static void bugTurn() throws GameActionException {
		if (detectBugIntoEdge()) {
			reverseBugWallFollowDir();
		}
		Direction dir = findBugMoveDir();
		if (dir != null) {
			bugMove(dir);
		}
	}

	private static Direction findBugMoveDir() throws GameActionException {
		bugMovesSinceSeenObstacle++;
		Direction dir = bugLastMoveDir;
		for (int i = 18; i-- > 0;) {
			if (canMove(dir))
				return dir;
			dir = (bugWallSide == WallSide.LEFT ? dir.rotateRightDegrees(20) : dir.rotateLeftDegrees(20));
			if(i < 17)
				bugMovesSinceSeenObstacle = 0;
		}
		return null;
	}

	private static void bugMove(Direction dir) throws GameActionException {
		if (tryMove(dir,type.strideRadius,true) == 0) {
			bugRotationCount += calculateBugRotation(dir);
			bugLastMoveDir = dir;
			if (bugWallSide == WallSide.LEFT)
				bugLookStartDir = dir.rotateLeftDegrees(90);
			else
				bugLookStartDir = dir.rotateRightDegrees(90);
		}
	}

	private static int calculateBugRotation(Direction moveDir) {
		if (bugWallSide == WallSide.LEFT) {
			return numRightRotations(bugLookStartDir, moveDir) - numRightRotations(bugLookStartDir, bugLastMoveDir);
		} else {
			return numLeftRotations(bugLookStartDir, moveDir) - numLeftRotations(bugLookStartDir, bugLastMoveDir);
		}
	}

	private static int numRightRotations(Direction start, Direction end) {
		return ((int) ((end.getAngleDegrees() - start.getAngleDegrees()) + 360) % 360) / 20;
	}

	private static int numLeftRotations(Direction start, Direction end) {
		return ((int) ((start.getAngleDegrees() - end.getAngleDegrees()) + 360) % 360) / 20;
	}

	private static boolean move(Direction dir) throws GameActionException {
		if (rc.canMove(dir, type.strideRadius)) {
			rc.move(dir);
			return true;
		}
		return false;
	}

	private static boolean detectBugIntoEdge() throws GameActionException {
		if (bugWallSide == WallSide.LEFT) {
			return !rc.onTheMap(here.add(bugLastMoveDir.rotateLeftDegrees(90)), type.strideRadius);
		} else {
			return !rc.onTheMap(here.add(bugLastMoveDir.rotateRightDegrees(90)), type.strideRadius);
		}
	}

	private static void reverseBugWallFollowDir() throws GameActionException {
		bugWallSide = (bugWallSide == WallSide.LEFT ? WallSide.RIGHT : WallSide.LEFT);
		startBug();
	}

	private static void startBug() throws GameActionException {
		bugStartDistSq = here.distanceSquaredTo(dest);
		bugLastMoveDir = here.directionTo(dest);
		bugLookStartDir = here.directionTo(dest);
		bugRotationCount = 0;
		bugMovesSinceSeenObstacle = 0;
		bugMovesSinceMadeProgress = 0;
		if (bugWallSide == null) {
			// try to intelligently choose on which side we will keep the wall
			Direction leftTryDir = bugLastMoveDir.rotateLeftDegrees(20);
			for (int i = 0; i < 9; i++) {
				if (!canMove(leftTryDir))
					leftTryDir = leftTryDir.rotateLeftDegrees(20);
				else
					break;
			}
			Direction rightTryDir = bugLastMoveDir.rotateRightDegrees(20);
			for (int i = 0; i < 9; i++) {
				if (!canMove(rightTryDir))
					rightTryDir = rightTryDir.rotateRightDegrees(20);
				else
					break;
			}
			if (dest.distanceSquaredTo(here.add(leftTryDir)) < dest.distanceSquaredTo(here.add(rightTryDir))) {
				bugWallSide = WallSide.RIGHT;
			} else {
				bugWallSide = WallSide.LEFT;
			}
		}

	}

	private static boolean canMove(Direction leftTryDir) {
		// TODO: add safety
		return rc.canMove(leftTryDir, type.strideRadius);
	}

	private static boolean tryMoveDirect() throws GameActionException {
		Direction dir = here.directionTo(dest);
		//System.out.println(dest.toString() + here.toString() + dir.toString());
		if (tryMove(dir, here.distanceTo(dest),true) == 0) {
			return true;
		}
		Direction left = dir.rotateLeftDegrees(15);
		Direction right = dir.rotateRightDegrees(15);
		for (int i = 0; i < 3; i++) {
			if (tryMove(left, here.distanceTo(dest),true) == 0) {
				return true;
			}
			if (tryMove(right, here.distanceTo(dest),true) == 0) {
				return true;
			}
			left = left.rotateLeftDegrees(15);
			right = right.rotateRightDegrees(15);
		}
		return false;
	}

	private static boolean canEndBug() {
		if (bugMovesSinceSeenObstacle >= 2)
			return true;
		if (debug) {
			System.out.println("bug rotation count = " + bugRotationCount);
			System.out.println("bugMovesSinceSeenObstacle = " + bugMovesSinceSeenObstacle);
		}
		return (bugRotationCount <= 0 || bugRotationCount >= 18) && here.distanceSquaredTo(dest) <= bugStartDistSq;
	}

	public static void explore() throws GameActionException {
		if (dirIAmMoving == null) {
			dirIAmMoving = here.directionTo(MapAnalysis.center);
		}
//		if (nearbyAlliedRobots != null) {
//			for (RobotInfo r : nearbyAlliedRobots) {
//				if (r.type == RobotType.SCOUT && dirIAmMoving.degreesBetween(here.directionTo(r.location)) < 45) {
//					dirIAmMoving = dirIAmMoving.opposite();
//					break;
//				}
//			}
//		}
		if (myRand.nextFloat() < 0.1 || !rc.onTheMap(here.add(dirIAmMoving, (float) (type.sensorRadius-.1)))) {
			// System.out.println(dirIAmMoving);
			dirIAmMoving = dirIAmMoving.rotateLeftDegrees(100);
		}
		goTo(dirIAmMoving);
	}

	/////////////////////////////// Dangerous Nav///////////////////////////////
	public static void goToDangerous(MapLocation loc) throws GameActionException {
		if (here.distanceTo(loc) < type.strideRadius && rc.canMove(loc)) {
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
		float theta = Math.abs(bullet.dir.radiansBetween(directionToRobot));
		if (theta > Math.PI / 2) {
			return false;
		}
		float distToRobot = bullet.location.distanceTo(loc);
		float perpendicularDist = (float) (distToRobot * Math.sin(theta));
		return (perpendicularDist < type.bodyRadius + .01 && (distToRobot - type.bodyRadius < bullet.speed + .01));
	}
}
