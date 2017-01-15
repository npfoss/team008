package team008.finalBot;

import battlecode.common.*;

public class MapAnalysis extends Bot {
	public static boolean startedGame = false;
	public static boolean isDecisionMaker = false;
	public static MapLocation center;
	public static MapLocation[] initialAlliedArchonLocations;
	public static MapLocation[] initialEnemyArchonLocations;
	public static int numArchon = 0;
	public static int numGardener = 0;
	public static int numSoldier = 0;
	public static int numTank = 0;
	public static int numScout = 0;
	public static int numLumberjack = 0;
	public static int numTree = 0;
	public static float minX;
	public static float minY;
	public static float maxX;
	public static float maxY;

	public static MapLocation findCenter() throws GameActionException {
		int archons = 0;
		float xavg = 0;
		float yavg = 0;
		initialAlliedArchonLocations = rc.getInitialArchonLocations(us);
		initialEnemyArchonLocations = rc.getInitialArchonLocations(enemy);
		for (MapLocation loc : initialAlliedArchonLocations) {
			xavg += loc.x;
			yavg += loc.y;
			archons++;
		}
		for (MapLocation loc : initialEnemyArchonLocations) {
			xavg += loc.x;
			yavg += loc.y;
			archons++;
		}
		return (new MapLocation(xavg / archons, yavg / archons));
	}

	/*
	 * Called everytime roundNum %10 == 1 updates unit counts
	 */
	public static void rollCall() throws GameActionException {
		switch (type) {
		case ARCHON:
			rc.broadcast(4, rc.readBroadcast(4) + 1);
			break;
		case GARDENER:
			rc.broadcast(5, rc.readBroadcast(5) + 1);
			break;
		case SOLDIER:
			rc.broadcast(6, rc.readBroadcast(6) + 1);
			break;
		case TANK:
			rc.broadcast(7, rc.readBroadcast(7) + 1);
			break;
		case SCOUT:
			rc.broadcast(8, rc.readBroadcast(8) + 1);
			break;
		case LUMBERJACK:
			rc.broadcast(9, rc.readBroadcast(9) + 1);
			break;
		default:
			break;

		}
	}

	public static void resetUnitCount() throws GameActionException {
		rc.broadcast(4, 0);
		rc.broadcast(5, 0);
		rc.broadcast(6, 0);
		rc.broadcast(7, 0);
		rc.broadcast(8, 0);
		rc.broadcast(9, 0);
	}

	public static void updateUnitCount() throws GameActionException {
		numArchon = rc.readBroadcast(4);
		numGardener = rc.readBroadcast(5);
		numSoldier = rc.readBroadcast(6);
		numTank = rc.readBroadcast(7);
		numScout = rc.readBroadcast(8);
		numLumberjack = rc.readBroadcast(9);
		numTree = rc.getTreeCount();
		System.out.println("numArchon = " + numArchon);
		System.out.println("numGardener = " + numGardener);
		System.out.println("numSoldier = " + numSoldier);
		System.out.println("numTank = " + numTank);
		System.out.println("numScout = " + numScout);
		System.out.println("numLumberjack = " + numLumberjack);
	}

	public static void updateMapSize() throws GameActionException {
		minX = Messaging.getMinX();
		minY = Messaging.getMinY();
		maxX = Messaging.getMaxX();
		maxY = Messaging.getMaxY();
	}

	public static void guessMapSize() throws GameActionException {
		minX = 600;
		maxX = 0;
		minY = 600;
		maxY = 0;
		for (MapLocation loc : initialAlliedArchonLocations) {
			if (loc.x < minX) {
				minX = loc.x;
			}
			if (loc.y < minY) {
				minY = loc.y;
			}
			if (loc.x > maxX) {
				maxX = loc.x;
			}
			if (loc.y > maxY) {
				maxY = loc.y;
			}
		}
		for (MapLocation loc : initialEnemyArchonLocations) {
			if (loc.x < minX) {
				minX = loc.x;
			}
			if (loc.y < minY) {
				minY = loc.y;
			}
			if (loc.x > maxX) {
				maxX = loc.x;
			}
			if (loc.y > maxY) {
				maxY = loc.y;
			}
		}
		Messaging.updateMinX(minX);
		Messaging.updateMinY(minY);
		Messaging.updateMaxX(maxX);
		Messaging.updateMaxY(maxY);
	}

	public static void possiblyMakeDecisions() throws GameActionException {
		if (rc.getRoundNum() % 25 == 0 || rc.getRoundNum() < 2 && isDecisionMaker == false) {
			isDecisionMaker = false;
			// +1 to account for the first round
			if (rc.readBroadcast(10) < rc.getRoundNum() + 1) {
				rc.broadcast(10, rc.getRoundNum() + 5);
				isDecisionMaker = true;
				resetUnitCount();
			}
		}
		if (!isDecisionMaker) {
			// good work
			return;
		} else {
			if (rc.getRoundNum() < 5 && !startedGame) {
				// rudimentary genetics strategy
				startedGame = true;
				guessMapSize();
				float TreesNearMe = 0;
				for (TreeInfo tree : nearbyNeutralTrees) {
					TreesNearMe ++;
				}
				float conflictDist = 999;
				for (MapLocation ourLoc : initialAlliedArchonLocations) {
					for (MapLocation theirLoc : initialEnemyArchonLocations) {
						float temp = ourLoc.distanceTo(theirLoc);
						if (temp < conflictDist) {
							conflictDist = temp;
						}
					}
				}
				if (conflictDist < 4) {
					rc.broadcast(11, 2);
				} else if (TreesNearMe > 20) {
					rc.broadcast(11, 3);
				} else {
					rc.broadcast(11, 4);
				}
				// rc.broadcast(12, 1);
				rc.broadcast(13, 1);
				rc.broadcast(15, 1);
				rc.broadcast(14, 3);
			} else if (rc.getRoundNum() % 25 == 2 && rc.getRoundNum() > 5) {
				updateUnitCount();
				updateMapSize();
				if (numGardener == 0 || rc.getTeamBullets() > 150 && numGardener == 1) {
					rc.broadcast(13, 1);
				}
				if (numScout < 1) {
					rc.broadcast(15, 1);
					rc.broadcast(14, 3);
				} else if (numLumberjack < ((rc.getTeamBullets() < 200 && numGardener < 6) ? ((rc.readBroadcast(11) == 3) ? 5:1) : 10)) {
					rc.broadcast(14, 4);
					rc.broadcast(15, ((rc.getTeamBullets() < 200 && numGardener < 6) ? ((rc.readBroadcast(11) == 3) ? 5:1) : 10) - numLumberjack);
				} else if (numSoldier < ((rc.getTeamBullets() < 200 && numGardener < 6) ? ((rc.readBroadcast(11) == 2) ? 5:1): 10)) {
					rc.broadcast(14, 1);
					rc.broadcast(15, ((rc.getTeamBullets() < 200 && numGardener < 6) ? ((rc.readBroadcast(11) == 2) ? 5:1) : 10) - numSoldier);
				} else if (numGardener < 10){
					rc.broadcast(13, 2);
					rc.broadcast(14, 5);
					
				}

			}

		}
	}
}