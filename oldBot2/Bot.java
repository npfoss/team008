package team008.oldBot2;

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
    
    //most units will need this
    public static Direction dirIAmMoving;
    public static MapLocation target;
    public static Direction calculatedMove;

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
    public static int bytecode;
    public static int roundNum;

    public Bot(){}

	public Bot(RobotController r) throws GameActionException {
		rc = r;
		type = rc.getType();
		enemy = rc.getTeam().opponent();
		us = rc.getTeam();
		here = rc.getLocation();
		myRand = new Random(rc.getID());
		dirIAmMoving = null;
		MapAnalysis.center = MapAnalysis.findCenter();
	}

	public void loop() {
		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				int temp = rc.getRoundNum();
				here = rc.getLocation();
				// TODO: have our Util sort a single call rather than calling
				// multiple times
                nearbyTrees = rc.senseNearbyTrees(-1);
                FastMethods.initializeNearbyNeutralTrees();
                FastMethods.initializeNearbyEnemyTrees();
                FastMethods.initializeNearbyAlliedTrees();
				nearbyRobots = rc.senseNearbyRobots(-1);
				FastMethods.initializeNearbyAlliedRobots();
				FastMethods.initializeNearbyEnemyRobots();
                nearbyBullets = rc.senseNearbyBullets();
                roundNum = rc.getRoundNum();
                if (roundNum + 5 > GameConstants.GAME_DEFAULT_ROUNDS
						|| rc.getTeamVictoryPoints() + rc.getTeamBullets() / 10 > 1000) {
					rc.donate(((int) (rc.getTeamBullets() / 10)) * 10);
				}
				MapAnalysis.possiblyMakeDecisions();
				strategy = rc.readBroadcast(11);
				if (roundNum % 25 == 1) {
					MapAnalysis.rollCall();
				}
				shakeNearbyTrees();
				if (nearbyEnemyTrees.length > 0 && (rc.getRoundNum() +rc.getID()) % 25 == 0) {
					Messaging.updateEnemyTreeLocation(nearbyEnemyTrees[0].location);
				}
				takeTurn();

				if (rc.canShake()) {
					// don't need to update nearbyNeutralTrees since
					// sensorRadius >>> strideRadius
					shakeNearbyTrees();
				}
				if(rc.getRoundNum() != temp){
					System.out.println("Oh shit we used way too many bytecodes");
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
				if (rc.getType() != RobotType.SCOUT) {
					//System.out.println("***A robot that isn't a scout just shook a tree!!!");
					return;
				}
			}
		}
	}

	public void assignNewTarget() throws GameActionException {
		target = Messaging.getClosestEnemyArmyLocation(here);
		if (target == null) {
			target = Messaging.getClosestEnemyUnitLocation(here);
		}
	}

    public static void notifyFriendsOfEnemies(RobotInfo[] enemies) throws GameActionException{
        if(enemies.length == 1){
            Messaging.updateEnemyUnitLocation(enemies[0].location);
        }
        else if (enemies.length > 1){
            Messaging.updateEnemyArmyLocation(Util.centroidOfUnits(enemies));
        }
    }

	/******* ALL NAVIGATION METHODS BELOW *******/
	// TODO: navigate/implement bugging
	private static MapLocation dest = null;
	private static boolean isBugging = false;

	protected static int dangerRating(MapLocation loc) {
		float danger = 0;
		for (BulletInfo b : nearbyBullets) {
			if (willCollide(b, loc)) {
				danger += b.damage * 10;
			}
		}
		boolean enemiesNearby = nearbyEnemyRobots.length > 0;
		if (type == RobotType.SCOUT) {
			boolean doneLumbers = false;
			boolean doneRangers = false;
			for (RobotInfo l : nearbyRobots) {
				if (l.team == us && l.type != RobotType.LUMBERJACK || l.type == RobotType.ARCHON
						|| l.type == RobotType.GARDENER) {

				} else if (l.type == RobotType.LUMBERJACK) {
					if (!doneLumbers) {
						if (Util.distanceSquaredTo(loc, l.location) < 21) {
							danger += (10.0 - loc.distanceTo(l.location)) * 50;
						} else {
							doneLumbers = true;
						}
					}
				} else {
					if (!doneRangers) {
						if (loc.distanceTo(l.location) < l.type.bodyRadius + l.type.strideRadius + l.type.bulletSpeed
								+ RobotType.SCOUT.bodyRadius) {

							danger += (10.0 - loc.distanceTo(l.location)) * 10.0 * l.type.attackPower;
						} else {
							doneRangers = true;
						}
					}
				}

			}
		} else {
			boolean doneEnemyLumbers = false;
			boolean doneOurLumbers = false;
			boolean doneEnemyRangers = false;
			boolean doneOurRangers = false;
			for (RobotInfo l : nearbyRobots) {
				if (l.team == enemy) {
					if (type != RobotType.LUMBERJACK) {
						if (l.type == RobotType.LUMBERJACK) {
							if (!doneEnemyLumbers) {
								if (Util.distanceSquaredTo(loc, l.location) < 21) {
									danger += (10.0 - loc.distanceTo(l.location)) * 50;
								} else {
									doneEnemyLumbers = true;
								}
							}
						} else if (l.type == RobotType.SCOUT || l.type == RobotType.SOLDIER
								|| l.type == RobotType.TANK) {
							if (!doneEnemyRangers) {
								if (loc.distanceTo(l.location) < l.type.bodyRadius + l.type.strideRadius
										+ l.type.bulletSpeed + type.bodyRadius) {
									danger += (10.0 - loc.distanceTo(l.location)) * 10.0 * l.type.attackPower;
								} else {
									doneEnemyRangers = true;
								}
							}
						}
					}
				} else {
					if (l.type == RobotType.LUMBERJACK) {
						if (!doneOurLumbers) {
							if (Util.distanceSquaredTo(loc, l.location) < 21) {
								danger += (10.0 - loc.distanceTo(l.location)) * 50.0;

							} else {
								doneOurLumbers = true;
							}
						}
					}
					if (enemiesNearby
							&& (l.type == RobotType.SOLDIER || l.type == RobotType.TANK || l.type == RobotType.SCOUT)) {
						if (!doneOurRangers) {
							MapLocation possibleShot = nearbyEnemyRobots[0].location;

							if (loc.directionTo(possibleShot)
									.radiansBetween(l.location.directionTo(possibleShot)) < Math.PI / 12
									&& loc.distanceTo(possibleShot) < l.location.distanceTo(possibleShot)) {
								danger += 10 * l.type.attackPower;
							} else {
								doneOurRangers = true;
							}
						}
					}
				}

			}
		}
		return (int) danger;
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

	public static boolean tryMoveDirection(Direction dir, boolean makeMove) throws GameActionException {
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
		Direction left = dir.rotateLeftDegrees(20);
		Direction right = dir.rotateRightDegrees(20);
		for (int i = 0; i < 9; i++) {

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
			left = left.rotateLeftDegrees(20);
			right = right.rotateRightDegrees(20);
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
		tryMoveDirection(dir, true);
	}

	public static void goTo(MapLocation theDest) throws GameActionException {
		// for now
		if (theDest == null) {
			tryMoveDirection(here.directionTo(MapAnalysis.center), true);
		}
		if (dest != null && dest.distanceTo(theDest) < .001) {
			// continue bugging
		} else {
			// no more bugging
			dest = theDest;
			isBugging = false;
		}

		if (!isBugging || 1 == 1) {
			
			if (tryMoveDirection(here.directionTo(dest), true)) {
				return;
			} else {
				isBugging = true;
				// for now we give up
				return;
			}
		}
	}

	public static void explore() throws GameActionException {
		if (dirIAmMoving == null) {
			dirIAmMoving = here.directionTo(MapAnalysis.center);
		}
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

    /**
     * Checks if hypothetically a lumberjack could hit this spot next turn.
     * @param loc
     * @param robot
     * @return true if a lumberjack could hit this spot next turn
     */
    private static boolean couldLumberJackHitLoc(MapLocation loc, RobotInfo robot){
        return loc.distanceTo(robot.location) < RobotType.LUMBERJACK.bodyRadius
                + RobotType.LUMBERJACK.strideRadius
                + GameConstants.LUMBERJACK_STRIKE_RADIUS;
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
				damageToSpot += robot.type.attackPower;
			}
		}

		for (RobotInfo robot : nearbyAlliedRobots) {
			if (robot.type == RobotType.LUMBERJACK && couldLumberJackHitLoc(loc, robot)) {
				damageToSpot += robot.type.attackPower;
			}
		}
		return damageToSpot;
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
		Direction directionToRobot = new Direction(bulletLocation,loc);
		float distToRobot = bulletLocation.distanceTo(loc);
		float theta = Math.abs(propagationDirection.radiansBetween(directionToRobot));

		// If theta > 90 degrees, then the bullet is traveling away from us and
		// we can break early
		if (theta > Math.PI / 2) {
			return false;
		}

		float perpendicularDist = (float) (distToRobot * Lookup.lookupSin(theta)); // soh
																					// cah
																					// toa
																					// :)

		// parallel distance not completely accurate but fast to calculate
		return (perpendicularDist <= type.bodyRadius
				&& (nearbyTrees.length > 3 ? true : (distToRobot - type.bodyRadius < bulletSpeed)));
	}
}