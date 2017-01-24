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
	public static float area;
	public static final int RUSH_VP = 1;
	public static final int RUSH_ENEMY = 2;
	public static final int CLEAR_TREES = 3;
	public static final int BUILD_TREES = 4;
	public static final int DEFEND_NOTHING = 1;
	public static final int DEFEND_SCOUT = 2;
	public static final int DEFEND_SOLDIER = 3;
	public static final int DEFEND_LUMBERJACK =4;
	public static final int SOLDIER = 1;
	public static final int TANK = 2;
	public static final int SCOUT = 3;
	public static final int LUMBERJACK = 4;
	public static final int TREE = 5;
	public static int adaptation =0;
	public static int genetics = 0;
	
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
		numArchon = Message.NUM_ARCHONS.getValue();
		numGardener = Message.NUM_GARDENERS.getValue();
		numSoldier = Message.NUM_SOLDIERS.getValue();
		numTank = Message.NUM_TANKS.getValue();
		numScout = Message.NUM_SCOUTS.getValue();
		numLumberjack = Message.NUM_LUMBERJACKS.getValue();
		numTree = rc.getTreeCount();
		adaptation = Message.ADAPTATION.getValue();
		genetics = Message.GENETICS.getValue();
		/*
		System.out.println("numArchon = " + numArchon);
		System.out.println("numGardener = " + numGardener);
		System.out.println("numSoldier = " + numSoldier);
		System.out.println("numTank = " + numTank);
		System.out.println("numScout = " + numScout);
		System.out.println("numLumberjack = " + numLumberjack);

		System.out.println("Archon BuildNum = " + rc.readBroadcast(13));
		System.out.println("Gardener BuildType = " + rc.readBroadcast(14));
		System.out.println("Gardener BuildNum = " + rc.readBroadcast(15));
		*/
	}

	public static void updateMapSize() throws GameActionException {
		area = Message.MAP_SIZE.getValue();
	}
	public static void determineInitialStrategy() throws GameActionException{
		startedGame = true;
		float TreesNearMe = nearbyNeutralTrees.length;
		float conflictDist = 999;
		for (MapLocation ourLoc : initialAlliedArchonLocations) {
			for (MapLocation theirLoc : initialEnemyArchonLocations) {
				float temp = ourLoc.distanceTo(theirLoc);
				if (temp < conflictDist) {
					conflictDist = temp;
				}
			}
		}
		if (conflictDist < 40) {
			Message.GENETICS.setValue(RUSH_ENEMY);
		} else if (TreesNearMe > 10 || true) {
			Message.GENETICS.setValue(CLEAR_TREES);
		} else {
			Message.GENETICS.setValue(BUILD_TREES);
		}
		
	}
	public static void makeDecisions() throws GameActionException {
		updateUnitCount();
		if(!startedGame && rc.getRoundNum() ==1){
			determineInitialStrategy();
		}
		if(GameConstants.GAME_DEFAULT_ROUNDS - rc.getRoundNum() < 10)
			rc.donate(rc.getTeamBullets());
		switch(genetics){
		case RUSH_VP:
			break;
		case RUSH_ENEMY:
			if (numGardener == 0 || rc.getRoundNum() > 200 && numGardener < 10
					&& (numGardener == 1 || numGardener * 4 < numTree || rc.getTeamBullets() > 150)) {
				Message.ARCHON_BUILD_NUM.setValue(1);
			}
			switch (adaptation) {
			case 0:
			case DEFEND_NOTHING:
			case DEFEND_SCOUT:
			case DEFEND_SOLDIER:
			case DEFEND_LUMBERJACK:
				if (numSoldier < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} /*else if (numScout < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SCOUT);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} */else if (numLumberjack < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(LUMBERJACK);
					Message.GARDENER_BUILD_NUM.setValue(5 - numLumberjack);
				} else if (numSoldier < 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(3 - numSoldier);
				} else if (numLumberjack < numSoldier && numLumberjack < 10) {
					Message.GARDENER_BUILD_ORDERS.setValue(LUMBERJACK);
					Message.GARDENER_BUILD_NUM.setValue(numGardener);
				} else if (numSoldier < 10) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(numGardener);
				}
				break;
			}
			break;
		case CLEAR_TREES:
			if (numGardener == 0 || rc.getRoundNum() > 200 && numGardener < 10
					&& (numGardener == 1 || numGardener * 4 < numTree || rc.getTeamBullets() > 150)) {
				Message.ARCHON_BUILD_NUM.setValue(1);
			}
			switch (adaptation) {
			case 0:
			case DEFEND_NOTHING:
			case DEFEND_SCOUT:
				if (numScout < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SCOUT);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} else if (numLumberjack < 5) {
					Message.GARDENER_BUILD_ORDERS.setValue(LUMBERJACK);
					Message.GARDENER_BUILD_NUM.setValue(5 - numLumberjack);
				} else if (numSoldier < 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(3 - numSoldier);
				} else if (numLumberjack < numSoldier && numLumberjack < 10) {
					Message.GARDENER_BUILD_ORDERS.setValue(LUMBERJACK);
					Message.GARDENER_BUILD_NUM.setValue(numGardener);
				} else if (numSoldier < 10) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(numGardener);
				}
				break;
			case DEFEND_SOLDIER:
			case DEFEND_LUMBERJACK:
				if (numSoldier < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} else if (numScout < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SCOUT);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} else if (numLumberjack < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(LUMBERJACK);
					Message.GARDENER_BUILD_NUM.setValue(5 - numLumberjack);
				} else if (numSoldier < 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(3 - numSoldier);
				} else if (numLumberjack < numSoldier && numLumberjack < 10) {
					Message.GARDENER_BUILD_ORDERS.setValue(LUMBERJACK);
					Message.GARDENER_BUILD_NUM.setValue(numGardener);
				} else if (numSoldier < 10) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(numGardener);
				}
				break;
			}
			break;
		case BUILD_TREES:			
			break;
		}

	}

}