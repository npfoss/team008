package team008.finalBot;

import java.util.Random;

import battlecode.common.*;

public class Bot {
	// for everyone to use
	public static RobotController rc;
	public static RobotType type;
	public static Team enemy;
	public static Team us;
	public static MapLocation here;

	// most units will need this
	public static Direction dirIAmMoving;
	public static MapLocation target;

	// finding these is expensive so lets only do it once
	public static TreeInfo[] nearbyNeutralTrees;
	public static TreeInfo[] nearbyEnemyTrees;
	public static TreeInfo[] nearbyAlliedTrees;
	public static RobotInfo[] nearbyAlliedRobots;
	public static RobotInfo[] nearbyEnemyRobots;
	public static BulletInfo[] nearbyBullets;
	public static RobotInfo[] nearbyRobots;
	public static Random myRand;
	public static int strategy = 0;

	public Bot() {
	}

	public Bot(RobotController r) throws GameActionException {
		rc = r;
		type = rc.getType();
		enemy = rc.getTeam().opponent();
		us = rc.getTeam();
		here = rc.getLocation();
		myRand = new Random(rc.getID());
		dirIAmMoving = Util.randomDirection();
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
				// TODO: have our Util sort a single call rather than calling
				// multiple times
				nearbyNeutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
				nearbyEnemyTrees = rc.senseNearbyTrees(-1, enemy);
				nearbyAlliedTrees = rc.senseNearbyTrees(-1, us);
				nearbyBullets = rc.senseNearbyBullets();
				nearbyRobots = rc.senseNearbyRobots(-1);
				nearbyAlliedRobots = rc.senseNearbyRobots(-1, us);
				nearbyEnemyRobots = rc.senseNearbyRobots(-1, enemy);
				if (rc.getRoundNum() + 5 > GameConstants.GAME_DEFAULT_ROUNDS
						|| rc.getTeamVictoryPoints() + rc.getTeamBullets() / 10 > 1000) {
					rc.donate(((int) (rc.getTeamBullets() / 10)) * 10);
				}
				MapAnalysis.possiblyMakeDecisions();
				strategy = rc.readBroadcast(11);
				if (rc.getRoundNum() % 25 == 1) {
					MapAnalysis.rollCall();
				}

				shakeNearbyTrees();
				takeTurn();

				if (rc.canShake()) {
					// don't need to update nearbyNeutralTrees since
					// sensorRadius >>> strideRadius
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

	public TreeInfo[] shakeNearbyTrees() throws Exception {
		TreeInfo shakeMe = Util.highestShakeableBulletTree(nearbyNeutralTrees);
		if (shakeMe != null) {
			rc.shake(shakeMe.getID());
			if (rc.getType() != RobotType.SCOUT) {
				System.out.println("***A robot that isn't a scout just shook a tree!!!");
			}
		}
		return nearbyNeutralTrees;
	}

	public void assignNewTarget() throws GameActionException {
		target = Messaging.getClosestEnemyArmyLocation(rc.getLocation());
		if (target == null) {
			target = Messaging.getClosestEnemyUnitLocation(rc.getLocation());
		}
	}

	/******* ALL NAVIGATION METHODS BELOW *******/
	// TODO: navigate/implement bugging
	private static MapLocation dest = null;
	private static boolean isBugging = false;

	private static int dangerRating(MapLocation loc) {
		int danger = 0;
		for (BulletInfo b : nearbyBullets) {
			if (willCollide(b, loc)) {
				danger += (int) (b.damage * 10);
			}
		}
		if (type == RobotType.SCOUT) {
			for (RobotInfo l : nearbyRobots) {
				if (l.team == us && l.type != RobotType.LUMBERJACK || l.type == RobotType.ARCHON
						|| l.type == RobotType.GARDENER) {

				} else if (l.type == RobotType.LUMBERJACK) {
					if (loc.distanceTo(l.location) < RobotType.LUMBERJACK.bodyRadius + RobotType.LUMBERJACK.strideRadius
							+ 1.1 + RobotType.SCOUT.bodyRadius) {
						danger += (10 - loc.distanceTo(l.location));
					}
				} else {
					if (loc.distanceTo(l.location) < l.type.bodyRadius + l.type.strideRadius + l.type.bulletSpeed
							+ RobotType.SCOUT.bodyRadius) {

						danger += (10 - loc.distanceTo(l.location));
					}
				}

			}
		} else {
			for (RobotInfo l : nearbyRobots) {
				if (l.team == enemy) {
					if (l.type == RobotType.LUMBERJACK && loc.distanceTo(l.location) < RobotType.LUMBERJACK.bodyRadius
							+ RobotType.LUMBERJACK.strideRadius + 1.1 + type.bodyRadius) {
						danger += (10 - loc.distanceTo(l.location));
					} else if (l.type == RobotType.SCOUT || l.type == RobotType.SOLDIER || l.type == RobotType.TANK) {
						if (loc.distanceTo(l.location) < l.type.bodyRadius + l.type.strideRadius + l.type.bulletSpeed
								+ type.bodyRadius && type != RobotType.LUMBERJACK) {
							danger += (10 - loc.distanceTo(l.location));
						}
					}
				} else {
					if (l.type == RobotType.LUMBERJACK
							&& loc.distanceTo(l.location) < RobotType.LUMBERJACK.bodyRadius + +1.1 + type.bodyRadius) {
						danger += (10 - loc.distanceTo(l.location));

					}
				}
			}
		}
		return danger;
	}

	private static int tryMove(Direction dir, float dist) throws GameActionException {
		int danger = dangerRating(here.add(dir, dist));
		if (rc.canMove(dir, dist) && danger == 0) {
			if (danger == 0) {
				rc.move(dir, dist);
				here = rc.getLocation();

			}
			return danger;
		}
		return 9999;

	}

	private static boolean tryMoveDirection(Direction dir) throws GameActionException {
		Direction bestDir = dir;
		int bestDanger = 9999;
		int tempDanger = 0;
		tempDanger = tryMove(dir, type.strideRadius);
		if (tempDanger == 0) {
			return true;
		}
		if (tempDanger < bestDanger) {
			bestDir = dir;
			bestDanger = tempDanger;
		}
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for (int i = 0; i < 17; i++) {

			tempDanger = tryMove(left, type.strideRadius);
			if (tempDanger == 0) {
				return true;
			}
			if (tempDanger < bestDanger) {
				bestDir = left;
				bestDanger = tempDanger;
			}
			tempDanger = tryMove(right, type.strideRadius);
			if (tempDanger == 0) {
				return true;
			}
			if (tempDanger < bestDanger) {
				bestDir = right;
				bestDanger = tempDanger;
			}
			left = left.rotateLeftDegrees(10);
			right = right.rotateRightDegrees(10);
		}
		tempDanger = dangerRating(here);
		if (tempDanger < bestDanger) {
			bestDir = null;
			bestDanger = tempDanger;
		}
		if (bestDir != null) {
			rc.move(bestDir, type.strideRadius);
			return true;
		}
		return false;
	}

	public static void goTo(Direction dir) throws GameActionException {
		tryMoveDirection(dir);
	}

	public static void goTo(MapLocation theDest) throws GameActionException {
		// for now
		if (theDest == null) {
			tryMoveDirection(here.directionTo(MapAnalysis.center));
		}
		if (dest != null && dest.distanceTo(theDest) < .001) {
			// continue bugging
		} else {
			// no more bugging
			dest = theDest;
			isBugging = false;
		}

		if (!isBugging || 1 == 1) {
			if (here.distanceTo(dest) < type.strideRadius
					&& rc.canMove(here.directionTo(dest), here.distanceTo(dest))) {
				rc.move(here.directionTo(dest), here.distanceTo(dest));
				here = rc.getLocation();
				return;
			}
			if (tryMoveDirection(here.directionTo(dest))) {
				return;
			} else {
				isBugging = true;
				// for now we give up
				return;
			}
		}
	}

	public static void explore() throws GameActionException {

		if (nearbyAlliedRobots != null) {
			for (RobotInfo r : nearbyAlliedRobots) {
				if (r.type == RobotType.SCOUT && dirIAmMoving.degreesBetween(here.directionTo(r.location)) < 45) {
					dirIAmMoving = dirIAmMoving.opposite();
					break;
				}
			}
		}
		if (myRand.nextFloat() < 0.1) {
			// System.out.println(dirIAmMoving);
			dirIAmMoving = dirIAmMoving.rotateLeftDegrees(100);
		}
		goTo(dirIAmMoving);
	}

	/////////////////////////////// Dangerous Nav///////////////////////////////
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

	/////////////////// Danger Calculation Tools/////////////
	/**
	 * This takes into account only hypothetical damage to this spot.
	 * 
	 * @param loc
	 */
	public static int hypotheticalDamageToSpot(MapLocation loc) {

		int damageToSpot = 0;
		for (RobotInfo robot : nearbyEnemyRobots) {
			if (robot.type != RobotType.LUMBERJACK) {
				if (couldHitLocNextTurn(loc, robot)) {
					damageToSpot += robot.type.attackPower;

				}
			} else if (couldLumberJackHitLoc(loc, robot)) {
				damageToSpot += robot.getType().attackPower;
			}
		}

		for (RobotInfo robot : nearbyAlliedRobots) {
			if (robot.type == RobotType.LUMBERJACK && couldLumberJackHitLoc(loc, robot)) {
				damageToSpot += robot.type.attackPower / 2;
			}
		}

		return damageToSpot;

	}

	/**
	 * Checks if hypothetically a lumberjack could hit this spot next turn.
	 * 
	 * @param loc
	 * @param robot
	 * @return true if a lumberjack could hit this spot next turn
	 */
	private static boolean couldLumberJackHitLoc(MapLocation loc, RobotInfo robot) {
		return (loc.distanceTo(robot.location) < RobotType.LUMBERJACK.bodyRadius + RobotType.LUMBERJACK.strideRadius + 1 // lumber
																															// jack
																															// swing
																															// radius

				+ type.bodyRadius);

	}

	/**
	 * Checks if after a move and shot the bullet could hit the loc.
	 * 
	 * @param loc
	 * @param robot
	 * @return True if it could hit us
	 */
	private static boolean couldHitLocNextTurn(MapLocation loc, RobotInfo robot) {
		if (robot.type == RobotType.ARCHON || robot.type == RobotType.GARDENER) {
			return false;
		}
		return (loc.distanceTo(robot.location) - robot.type.strideRadius - robot.type.bulletSpeed
				- robot.type.bodyRadius <= rc.getType().bodyRadius);
	}

	public static int knownDamageToLoc(MapLocation loc) {
		int damage = 0;
		for (BulletInfo bullet : nearbyBullets) {
			if (bulletWillHitLoc(loc, bullet)) {
				damage += bullet.damage;
			}
		}

		for (RobotInfo robot : nearbyEnemyRobots) {
			if (isLumberJackAndCanHitMe(loc, robot)) {
				damage += robot.type.attackPower;
			}
		}
		return damage;
	}

	private static boolean isLumberJackAndCanHitMe(MapLocation loc, RobotInfo robot) {
		return robot.type == RobotType.LUMBERJACK && loc.distanceTo(robot.location) < 1;
	}

	private static boolean bulletWillHitLoc(MapLocation loc, BulletInfo bullet) {
		return bullet.location.add(bullet.dir, bullet.speed).distanceTo(loc) < type.bodyRadius;
	}

	private static boolean willCollide(BulletInfo bullet, MapLocation loc) {

		// TODO: check if bullet will hit something else first
		// Get relevant bullet information
		Direction propagationDirection = bullet.dir;
		MapLocation bulletLocation = bullet.location;
		float bulletSpeed = bullet.speed;
		// Calculate bullet relations to this robot
		Direction directionToRobot = bulletLocation.directionTo(loc);
		float distToRobot = bulletLocation.distanceTo(loc);
		float theta = propagationDirection.radiansBetween(directionToRobot);

		// If theta > 90 degrees, then the bullet is traveling away from us and
		// we can break early
		if (Math.abs(theta) > Math.PI / 2) {
			return false;
		}
		float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh
																					// cah
																					// toa
																					// :)
		// parallel distance not completely accurate but fast to calculate
		return (perpendicularDist <= type.bodyRadius && distToRobot - type.bodyRadius < bulletSpeed);
	}
}
