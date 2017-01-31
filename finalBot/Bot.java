package team008.finalBot;

import java.util.Random;
import battlecode.common.*;

public class Bot {
	// for debugging
	public static boolean debug = true;

	// for everyone to use
	public static RobotController rc;
	public static RobotType type;
	public static Team enemy;
	public static Team us;
	public static MapLocation here;
	public static Direction calculatedMove;
	public static float TOLERANCE = .1f;

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
	public static BugState bugState;
	private static WallSide bugWallSide = null;

	public Bot() {
	}

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
		if (nearbyAlliedRobots.length > 0) {
			RobotInfo closestG = Util.closestSpecificType(nearbyAlliedRobots, here, RobotType.GARDENER);
			RobotInfo closestA = Util.closestSpecificType(nearbyAlliedRobots, here, RobotType.ARCHON);
			switch (type) {
			case ARCHON:
				Message.NUM_ARCHONS.setValue(Message.NUM_ARCHONS.getValue() + 1);
				break;
			case GARDENER:
				if (closestA == null
						|| here.distanceTo(closestA.location) > type.bodyRadius + RobotType.ARCHON.bodyRadius + 2) {
					Message.NUM_GARDENERS.setValue(Message.NUM_GARDENERS.getValue() + 1);
				}
				break;
			case SOLDIER:
				if (closestG == null
						|| here.distanceTo(closestG.location) > type.bodyRadius + RobotType.GARDENER.bodyRadius + 2) {
					Message.NUM_SOLDIERS.setValue(Message.NUM_SOLDIERS.getValue() + 1);
				}
				break;
			case TANK:
				if (closestG == null
						|| here.distanceTo(closestG.location) > type.bodyRadius + RobotType.GARDENER.bodyRadius + 2) {
					Message.NUM_TANKS.setValue(Message.NUM_TANKS.getValue() + 1);
				}
				break;
			case SCOUT:
				if (closestG == null
						|| here.distanceTo(closestG.location) > type.bodyRadius + RobotType.GARDENER.bodyRadius + 2) {
					Message.NUM_SCOUTS.setValue(Message.NUM_SCOUTS.getValue() + 1);
				}
				break;
			case LUMBERJACK:
				if (closestG == null
						|| here.distanceTo(closestG.location) > type.bodyRadius + RobotType.GARDENER.bodyRadius + 2) {
					Message.NUM_LUMBERJACKS.setValue(Message.NUM_LUMBERJACKS.getValue() + 1);
				}
				break;
			default:
				break;
			}
		}
	}

	enum BugState {
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
				if (!isDead && rc.getHealth() < (type == RobotType.SCOUT ? 0 : 9)) {
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
						if (Gardener.trapped) {
							Message.GARDENER_TRAPPED_NUM.setValue(Message.GARDENER_TRAPPED_NUM.getValue() - 1);
						}
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
				if (roundNum + 5 > GameConstants.GAME_DEFAULT_ROUNDS
						|| rc.getTeamVictoryPoints() + rc.getTeamBullets() / rc.getVictoryPointCost() >= 1000) {
					rc.donate(((int) (rc.getTeamBullets() / rc.getVictoryPointCost())) * rc.getVictoryPointCost());
				}
				if (rc.getTeamBullets() > 1000) {
					rc.donate(rc.getVictoryPointCost());
				}
				if (Message.GENETICS.getValue() == MapAnalysis.RUSH_VP && rc.getTeamBullets() > 500) {
					rc.donate(rc.getVictoryPointCost());
				}
				shakeNearbyTrees();
				takeTurn();
				if(Clock.getBytecodesLeft() > 500){
					if (rc.canShake()) {
						shakeNearbyTrees();
					}
					MapLocation gardenerBuildLoc = Message.GARDENER_BUILD_LOCS.getClosestLocation(here);
					if (gardenerBuildLoc != null)
						removeLocIfApplicable(gardenerBuildLoc);
				}
				if (rc.getRoundNum() != roundNum)
					System.out.println("******SHITSHITSHITSHITSHIT RAN OUT OF BYTECODE******");
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

	public void removeLocIfApplicable(MapLocation targetLoc) throws GameActionException {
		float dist = here.distanceTo(targetLoc);
		if((rc.canSenseLocation(targetLoc) && rc.senseRobotAtLocation(targetLoc) != null && rc.senseRobotAtLocation(targetLoc).type != RobotType.ARCHON && rc.senseRobotAtLocation(targetLoc).type != RobotType.GARDENER) || !rc.canSenseLocation(targetLoc)){
			return;
		}
		if(
		(!rc.onTheMap(targetLoc)
		|| isCircleOccupiedByTree(targetLoc, 2) || rc.senseRobotAtLocation(targetLoc) != null && ((rc.senseRobotAtLocation(targetLoc).type == RobotType.GARDENER && rc.senseNearbyTrees(targetLoc, (float)(2.5), us).length > 0))
		)){
			Message.GARDENER_BUILD_LOCS.removeLocation(targetLoc);
		}
		if(edgesOfSpotAreOffMap(targetLoc, RobotType.GARDENER.bodyRadius) || isntLastHopeAndSucks(targetLoc)){
            Message.GARDENER_BUILD_LOCS.removeLocation(targetLoc);
        }
	}

    private boolean isntLastHopeAndSucks(MapLocation targetLoc) throws GameActionException{
        if(Message.GARDENER_BUILD_LOCS.getLength() > 1){
            return edgesOfSpotAreOffMap(targetLoc, 3);
        }
        return false;
    }

    public boolean edgesOfSpotAreOffMap(MapLocation loc, float radius) throws GameActionException{
	    if(rc.canSenseLocation(loc)) {
            for (int i = 0; i < 4; i++) {
                MapLocation spot = loc.add( new Direction((float) ((Math.PI / 2) * i)),radius);
                if ( rc.canSenseLocation(spot) && !rc.onTheMap(spot)) {
                    return true; //its off the map
                }
            }
        }
        return false;
    }
	
	public boolean isCircleOccupiedByTree(MapLocation targetLoc, float i) throws GameActionException {
		if(!rc.canSenseAllOfCircle(targetLoc, i)){
			return rc.isLocationOccupiedByTree(targetLoc);
		}
		if(!rc.isCircleOccupied(targetLoc, i))
			return false;
		for(TreeInfo t: nearbyTrees){
			if(t.location.distanceTo(targetLoc) - t.radius < i){
				return true;
			}
		}
		return false;
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
	public static void assignNewTarget() throws GameActionException {
		if(debug)System.out.println(Message.DISTRESS_SIGNALS.getLength());
		if(debug)System.out.println(Message.ENEMY_ARMIES.getLength());
		if(debug)System.out.println(Message.ISOLATED_ENEMIES.getLength());
		target = null;
		MapLocation targetD = Message.DISTRESS_SIGNALS.getClosestLocation(here);
		if (targetD != null) {
			if (debug) {
				// System.out.println("got target D");
			}
		}
		target = Message.ENEMY_ARMIES.getClosestLocation(here);
		//System.out.println(target);
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
		RobotInfo closestG = Util.closestSpecificType(nearbyAlliedRobots, here, RobotType.GARDENER);
		if(isCircleOccupiedbyNeutralTree(enemies[0].location, enemies[0].type.bodyRadius))
			return;
		if (closestG != null && enemies[0].type != RobotType.ARCHON && closestG.location.distanceTo(enemies[0].location) < (enemies[0].type == RobotType.SCOUT ? 4: 7)) {
			Message.DISTRESS_SIGNALS.addLocation(Util.midpoint(closestG.location, enemies[0].location));
		}
		if (Util.closestSpecificType(nearbyEnemyRobots, here, RobotType.ARCHON) != null) {
			Message.ENEMY_ARCHONS
					.addLocation(Util.closestSpecificType(nearbyEnemyRobots, here, RobotType.ARCHON).location);
		}
		if(enemies.length == 1 && enemies[0].type != RobotType.SCOUT && enemies[0].type != RobotType.ARCHON){
			if(enemies[0].type == RobotType.GARDENER){
				Message.ENEMY_ARMIES.addLocation(enemies[0].location);
			}
			else{
				Message.ISOLATED_ENEMIES.addLocation(enemies[0].location);
			}
		} else if (enemies.length > 1) {
			for (RobotInfo e : nearbyEnemyRobots) {
				if (e.type != RobotType.SCOUT) {
					Message.ENEMY_ARMIES.addLocation(e.location);
					if(debug){
						if(debug)rc.setIndicatorLine(here, e.location, 0, 0, 255); 
						if(debug)System.out.println("adding");
					}
					break;
				}
			}
		}
	}

	private static boolean isCircleOccupiedbyNeutralTree(MapLocation targetLoc, float i) throws GameActionException {
		if(!rc.canSenseLocation(targetLoc)){
			return false;
		}
		if(!rc.canSenseAllOfCircle(targetLoc, i)){
			return rc.isLocationOccupiedByTree(targetLoc);
		}
		if(!rc.isCircleOccupied(targetLoc, i))
			return false;
		for(TreeInfo t: nearbyNeutralTrees){
			if(t.location.distanceTo(targetLoc) - t.radius < i){
				return true;
			}
		}
		return false;
	}

	/******* ALL NAVIGATION METHODS BELOW *******/
	private static MapLocation dest = null;
	// private static boolean isBugging = false;
	private static int bugRotationCount;

	private static float bugStartDistSq;

	private static int bugMovesSinceSeenObstacle;

	private static Direction bugLastMoveDir;

	private static Direction bugLookStartDir;

	private static int bugMovesSinceMadeProgress;

	private static int bugStartTurn;

	protected static int dangerRating(MapLocation loc) {
		float danger = 0;
		for (BulletInfo b : nearbyBullets) {
			if (willCollide(b, loc)) {
				danger += 10;
			}
		}
		boolean enemiesNearby = nearbyEnemyRobots.length > 0;
		if (type == RobotType.SCOUT) {
			for (RobotInfo l : nearbyRobots) {
				if (l.team == us && l.type != RobotType.LUMBERJACK || l.type == RobotType.ARCHON
						|| l.type == RobotType.GARDENER) {

				} else if (l.type == RobotType.LUMBERJACK) {
					if (loc.distanceTo(l.location) < RobotType.LUMBERJACK.bodyRadius + RobotType.LUMBERJACK.strideRadius*2
							+ 1.1 + RobotType.SCOUT.bodyRadius && (l.team == enemy || enemiesNearby)) {
						danger += (10.0 - loc.distanceTo(l.location)) * 10;
					}

				} else {
					if (loc.distanceTo(l.location) < l.type.bodyRadius + l.type.strideRadius + l.type.bulletSpeed*2
							+ RobotType.SCOUT.bodyRadius+.1) {

						danger += (10.0 - loc.distanceTo(l.location)) * 10.0 * l.type.attackPower;
					}
				}

			}
		} else {
			for (RobotInfo l : nearbyRobots) {
				if (l.team == enemy && type != RobotType.LUMBERJACK && l.type == RobotType.LUMBERJACK) {
					if (loc.distanceTo(l.location) < 3.51 + type.bodyRadius + (type == RobotType.SCOUT ? 2 : 0)) {
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
		}
		return (int) (danger);
	}


	private static int tryMove(Direction dir, float dist, boolean makeMove, boolean tryBinary) throws GameActionException {
		if (rc.canMove(dir, dist) && !(type == RobotType.TANK
				&& rc.isCircleOccupiedExceptByThisRobot(here.add(dir, dist), type.bodyRadius)
				&& (nearbyTrees.length > 0 && nearbyTrees[0].team == us))) {
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
		return (tryBinary ? tryMoveBinary(dir, dist, makeMove) : 9999);
	}
	
	private static int tryMoveBinary(Direction dir, float dist, boolean makeMove) throws GameActionException {
		float highDist = dist;
		float lowDist = 0;
		float midDist = (float)(dist/2);
		while(highDist - lowDist > .01){
			midDist = (highDist + lowDist) / 2;
			if (rc.canMove(dir, midDist) && !(type == RobotType.TANK && rc.isCircleOccupiedExceptByThisRobot(here.add(dir, dist), type.bodyRadius) && (nearbyTrees.length > 0 && nearbyTrees[0].team == us))) {
				int danger = 0;
				danger = dangerRating(here.add(dir, midDist));
				if (danger == 0) {
					lowDist = midDist;
				}
				else{
					highDist = midDist;
				}
			}
			else{
				highDist = midDist;
			}
		}
		if(rc.canMove(dir, (float)(midDist - .005))){
			calculatedMove = dir;
			if (makeMove) {
				rc.move(dir, (float)(midDist - .005));
				here = rc.getLocation();
			}
			return 0;
		}
		return 9999;
	}


	public static boolean tryMoveDirection(Direction dir, boolean makeMove, boolean goBackwards)
			throws GameActionException {
//		if (debug) System.out.println("trying to move in dir " + dir);

		Direction bestDir = dir;
		int bestDanger = tryMove(dir, type.strideRadius, makeMove, false);
		int tempDanger = 0;
		if (bestDanger == 0) {
			return true;
		}
		Direction left = dir.rotateLeftDegrees(30);
		Direction right = dir.rotateRightDegrees(30);
		for (int i = 0; i < (goBackwards ? 6 : 3); i++) {

			tempDanger = tryMove(left, type.strideRadius, makeMove, false);
			if (tempDanger == 0) {
				return true;
			}
			if (tempDanger < bestDanger) {
				bestDir = left;
				bestDanger = tempDanger;
			}
			tempDanger = tryMove(right, type.strideRadius, makeMove, false);
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
		if (bestDir != null) {
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
		if (theDest == null) {
			tryMoveDirection(here.directionTo(MapAnalysis.center), true, true);
			return;
		}
		if(here.distanceTo(theDest) < type.strideRadius && rc.canMove(here.directionTo(theDest), here.distanceTo(theDest))){
			rc.move(here.directionTo(theDest), here.distanceTo(theDest));
			return;
		}
		if (rc.getMoveCount() == 1 || here.distanceTo(theDest) < .1) {
			bugState = BugState.DIRECT;
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
			// System.out.println("bugging");
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
			// if(debug)System.out.println("still bugging");
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
		if(canMove(dir, false) && roundNum - bugStartTurn > 0 && !rc.canMove(bugWallSide == WallSide.LEFT ? dir.rotateLeftDegrees(100) : dir.rotateRightDegrees(100), type.strideRadius)){
			//stay on the wall
			//if(debug)System.out.println("staying on the wall");
			dir = (bugWallSide == WallSide.LEFT ? dir.rotateLeftDegrees(90) : dir.rotateRightDegrees(90));
			for (int i = 18; i-- > 0;) {
				if (canMove(dir, true))
					return dir;
				dir = (bugWallSide == WallSide.LEFT ? dir.rotateRightDegrees(5) : dir.rotateLeftDegrees(5));
				if(debug)rc.setIndicatorDot(here.add(dir, type.strideRadius), 255, 0, 0);
			}
		}
		bugMovesSinceSeenObstacle = 0;
		// see an obstacle
		for (int i = 72; i-- > 0;) {
			//if(debug)System.out.println("saw an obstacle");
			//if(debug)rc.setIndicatorDot(here.add(dir, type.strideRadius), 255, 0, 0);
			dir = (bugWallSide == WallSide.LEFT ? dir.rotateRightDegrees(5) : dir.rotateLeftDegrees(5));
			if (canMove(dir, true))
				return dir;
		}
		return null;
	}

	private static void bugMove(Direction dir) throws GameActionException {
		if(tryMove(dir, type.strideRadius, true, true) == 0){
			bugLastMoveDir = dir;
		}
		/*
		 * if (tryMove(dir,type.strideRadius,true) == 0) { bugRotationCount +=
		 * calculateBugRotation(dir); if (bugWallSide == WallSide.LEFT)
		 * bugLookStartDir = dir.rotateLeftDegrees(90); else bugLookStartDir =
		 * dir.rotateRightDegrees(90); }
		 */
	}

	private static int calculateBugRotation(Direction moveDir) {
		if (bugWallSide == WallSide.LEFT) {
			return numRightRotations(bugLookStartDir, moveDir) - numRightRotations(bugLookStartDir, bugLastMoveDir);
		} else {
			return numLeftRotations(bugLookStartDir, moveDir) - numLeftRotations(bugLookStartDir, bugLastMoveDir);
		}
	}

	private static int numRightRotations(Direction start, Direction end) {
		int endOrd = (int) ((end.getAngleDegrees() > 0 ? end.getAngleDegrees() : 360 + end.getAngleDegrees())) / 20;
		int startOrd = (int) ((start.getAngleDegrees() > 0 ? start.getAngleDegrees() : 360 + end.getAngleDegrees()))
				/ 20;
		return (endOrd - startOrd + 18) % 18;
	}

	private static int numLeftRotations(Direction start, Direction end) {
		int endOrd = (int) ((end.getAngleDegrees() > 0 ? end.getAngleDegrees() : 360 + end.getAngleDegrees())) / 20;
		int startOrd = (int) ((start.getAngleDegrees() > 0 ? start.getAngleDegrees() : 360 + end.getAngleDegrees()))
				/ 20;
		return (-endOrd + startOrd + 18) % 18;
	}

	private static boolean move(Direction dir) throws GameActionException {
		if (rc.canMove(dir, type.strideRadius) && !(type == RobotType.TANK
				&& rc.isCircleOccupiedExceptByThisRobot(here.add(dir, type.strideRadius), type.bodyRadius)
				&& (nearbyTrees.length > 0 && nearbyTrees[0].team == us))) {
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
		bugStartTurn = roundNum;
		bugRotationCount = 0;
		bugMovesSinceSeenObstacle = 0;
		bugMovesSinceMadeProgress = 0;
		if (bugWallSide == null) {
			// try to intelligently choose on which side we will keep the wall
			Direction leftTryDir = bugLastMoveDir.rotateLeftDegrees(20);
			for (int i = 0; i < 9; i++) {
				if (!canMove(leftTryDir, true))
					leftTryDir = leftTryDir.rotateLeftDegrees(20);
				else
					break;
			}
			Direction rightTryDir = bugLastMoveDir.rotateRightDegrees(20);
			for (int i = 0; i < 9; i++) {
				if (!canMove(rightTryDir, true))
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

	private static boolean canMove(Direction dir, boolean fullStride) throws GameActionException {
		// TODO: add safety
		return rc.canMove(dir, type.strideRadius) && !(type == RobotType.TANK
				&& rc.isCircleOccupiedExceptByThisRobot(here.add(dir, type.strideRadius), type.bodyRadius)
				&& (nearbyTrees.length > 0 && nearbyTrees[0].team == us));
	}

	private static boolean tryMoveDirect() throws GameActionException {
		Direction dir = here.directionTo(dest);
		//System.out.println(dest.toString() + here.toString() + dir.toString());
		if (tryMove(dir, type.strideRadius, true, false) == 0) {
			return true;
		}
		Direction left = dir.rotateLeftDegrees(15);
		Direction right = dir.rotateRightDegrees(15);
		for (int i = 0; i < 5; i++) {
			if (tryMove(left, type.strideRadius, true, false) == 0) {
				return true;
			}
			if (tryMove(right, type.strideRadius, true, false) == 0) {
				return true;
			}
			left = left.rotateLeftDegrees(15);
			right = right.rotateRightDegrees(15);
		}
		return false;
	}

	private static boolean canEndBug() {
		if (debug) {
			System.out.println("bugMovesSinceSeenObstacle = " + bugMovesSinceSeenObstacle);
			System.out.println("wall side = " + bugWallSide);
		}
		return bugMovesSinceSeenObstacle >= 4 && here.distanceSquaredTo(dest) <= bugStartDistSq + 2 || (nearbyTrees.length == 0 || here.distanceTo(nearbyTrees[0].location) > 4);
		// if (bugMovesSinceSeenObstacle >= 4)
		// return true;
		// return (bugRotationCount <= 0 || bugRotationCount >= 18) &&
		// here.distanceSquaredTo(dest) <= bugStartDistSq;
	}

	public static void explore() throws GameActionException {
		if (dirIAmMoving == null) {
			dirIAmMoving = here.directionTo(MapAnalysis.center);
		}
		// if (nearbyAlliedRobots != null) {
		// for (RobotInfo r : nearbyAlliedRobots) {
		// if (r.type == RobotType.SCOUT &&
		// dirIAmMoving.degreesBetween(here.directionTo(r.location)) < 45) {
		// dirIAmMoving = dirIAmMoving.opposite();
		// break;
		// }
		// }
		// }
		if (myRand.nextFloat() < 0.1 || !rc.onTheMap(here.add(dirIAmMoving, (float) (type.sensorRadius - .1)))) {
			// System.out.println(dirIAmMoving);
			dirIAmMoving = dirIAmMoving.rotateLeftDegrees(100);
		}
		tryMoveDirection(dirIAmMoving,true,true);
	}

	/////////////////////////////// Dangerous Nav///////////////////////////////
	public static void goToDangerous(MapLocation loc) throws GameActionException {
		if (here.distanceTo(loc) < type.strideRadius && rc.canMove(loc)
				&& !(type == RobotType.TANK && rc.isCircleOccupiedExceptByThisRobot(loc, type.bodyRadius)
						&& (nearbyTrees.length > 0 && nearbyTrees[0].team == us))) {
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
		if (rc.canMove(dir, dist) && !(type == RobotType.TANK
				&& rc.isCircleOccupiedExceptByThisRobot(here.add(dir, type.strideRadius), type.bodyRadius)
				&& (nearbyTrees.length > 0 && nearbyTrees[0].team == us))) {
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
		float distToRobot = bullet.location.distanceTo(loc);
		if (theta > Math.PI / 2 && distToRobot > type.bodyRadius+.01) {
			return false;
		}
		float perpendicularDist = (float) (distToRobot * Math.sin(theta));
		return (perpendicularDist < type.bodyRadius + .01 && (distToRobot - type.bodyRadius < bullet.speed + .01));
	}
}
