package team008.soldierBot00;

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
	public static float area;

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

	public static void updateUnitCount() throws GameActionException {
		numArchon = rc.readBroadcast(4);
		numGardener = rc.readBroadcast(5);
		numSoldier = rc.readBroadcast(6);
		numTank = rc.readBroadcast(7);
		numScout = rc.readBroadcast(8);
		numLumberjack = rc.readBroadcast(9);
		numTree = rc.getTreeCount();
	}

	public static void updateMapSize() throws GameActionException {
		area = rc.readBroadcast(16);
	}

	public static void makeDecisions() throws GameActionException {
		updateUnitCount();
		if (numGardener == 0 || rc.getTeamBullets() > 110 && rc.getRoundNum() > 50 && numGardener < 10) {
			rc.broadcast(13, 1);
		} else if (numScout < 0) {
			rc.broadcast(15, 1);
			rc.broadcast(14, 3);
		} else if (numLumberjack < 5) {
			rc.broadcast(14, 4);
			rc.broadcast(15, 5 - numLumberjack);
		} else if (numGardener * numGardener < numLumberjack) {
			rc.broadcast(13, numLumberjack - numGardener * numGardener);
			rc.broadcast(14, 4);
		} else if (numLumberjack < 20) {
			rc.broadcast(14, 4);
			rc.broadcast(15, 20 - numLumberjack);

		}

	}

}