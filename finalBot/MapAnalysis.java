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
	public static int initialSoldiers = 0;
	public static int initialLumberjacks = 0;
	public static float treeToSoldierRatio = 0;
	public static float rushHeuristic;
	public static float minX;
	public static float minY;
	public static float maxX;
	public static float maxY;
	public static float area;
	public static float conflictDist;
	public static int lastTurnWithDistress;
	public static boolean shouldBuildScout;
	public static final int RUSH_VP = 1;
	public static final int RUSH_ENEMY = 2;
	public static final int CLEAR_TREES = 3;
	public static final int BUILD_TREES = 4;
	public static final int DEFEND_NOTHING = 1;
	public static final int DEFEND_SOMETHING = 2;
	public static final int CLEAR_AS_WELL = 3;
	public static final int SOLDIER = 1;
	public static final int TANK = 2;
	public static final int SCOUT = 3;
	public static final int LUMBERJACK = 4;
	public static final int TREE = 5;
	public static int adaptation = 0;
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
		updateMapSize();
		if(debug){
			
		  System.out.println("numArchon = " + numArchon); 
		  System.out.println("numGardener = " + numGardener); 
		  System.out.println("numSoldier = " + numSoldier); 
		  System.out.println("numTank = " + numTank);
		  System.out.println("numScout = " + numScout); 
		  System.out.println("numLumberjack = " + numLumberjack);
		  
		  System.out.println("Archon BuildNum = " + rc.readBroadcast(13));
		  System.out.println("Gardener BuildType = " + rc.readBroadcast(14));
		  System.out.println("Gardener BuildNum = " + rc.readBroadcast(15));
		}
		 
	}

	public static void updateMapSize() throws GameActionException {
		area = Message.MAP_SIZE.getValue();
	}

	public static void determineInitialStrategy() throws GameActionException {
		shouldBuildScout = false;
		float trappedHeuristic = 0;
		for(MapLocation m: rc.getInitialArchonLocations(enemy)){
			Message.ENEMY_ARMIES.addLocation(m);
		}
		if(tryMoveDirection(new Direction(0), false, true)){
			for(TreeInfo t: nearbyNeutralTrees){
				//if(here.distanceTo(t.location) - t.radius < 4)
				//	Message.CLEAR_TREES_PLEASE.addLocation(t.location);
				if(t.containedBullets > 0){
					shouldBuildScout = true;
				}
				float dist = here.distanceTo(t.location);
				trappedHeuristic += (float)((t.radius + 1) * (type.sensorRadius - dist));
			}
			//not surrounded
		}
		startedGame = true;
		//float combinedRadii = 0;
		//for(TreeInfo t: nearbyNeutralTrees){
		//	combinedRadii += t.radius;
		//}
		//float treesNearMe = nearbyNeutralTrees.length;
		float largestConflictDist = -1;
		float conflictDistance = 999;
		for (MapLocation ourLoc : initialAlliedArchonLocations) {
			for (MapLocation theirLoc : initialEnemyArchonLocations) {
				float temp = ourLoc.distanceTo(theirLoc);
				if (temp < conflictDistance) {
					conflictDistance = temp;
				}
			}
			if(conflictDistance > largestConflictDist){
				largestConflictDist = conflictDistance;
			}
			conflictDistance = 999;
		}
		conflictDist = largestConflictDist;
		Message.CONFLICT_DIST.setValue(conflictDist);
		int treesToClear = Message.CLEAR_TREES_PLEASE.getLength();
		rushHeuristic = 110 - conflictDist - trappedHeuristic/(float)(2.0);
		initialSoldiers = (int)(rushHeuristic < 50 ? 0 : rushHeuristic < 90 ? 1 : 2);
		treeToSoldierRatio = (120 - rushHeuristic)/(float)(50.0);
		if(debug)System.out.println("Rush Heuristic = " + rushHeuristic + " initialSoldiers = " + initialSoldiers + "tree to soldier ratio = " + treeToSoldierRatio + " trapped heuristic = " + trappedHeuristic);
		if (rushHeuristic > 50) {
			Message.GENETICS.setValue(RUSH_ENEMY);
		}	
		else if (trappedHeuristic > 25) {
			Message.GENETICS.setValue(CLEAR_TREES);
		} else {
			Message.GENETICS.setValue(BUILD_TREES);
		}

	}

	public static void makeDecisions() throws GameActionException {
		updateUnitCount();
		int numUnitTrees = Message.TREES_WITH_UNITS.getLength();
		if (!startedGame && rc.getRoundNum() < 5) {
			determineInitialStrategy();
		}
		int treesToClear = Message.CLEAR_TREES_PLEASE.getLength();
		if(debug)System.out.println("treesToClear = " + treesToClear);
		int numEnemies = Message.ISOLATED_ENEMIES.getLength() + Message.ENEMY_ARMIES.getLength();
		if(rc.getTreeCount() > 20 && Message.ADAPTATION.getValue() == DEFEND_NOTHING && numEnemies < 10){
			Message.GENETICS.setValue(RUSH_VP);
		}
		else if (numEnemies > 9 && Message.GENETICS.getValue() == RUSH_VP){
			Message.GENETICS.setValue(treesToClear > 0 ? CLEAR_TREES: BUILD_TREES);
		}
		int numDistressSignals = Message.DISTRESS_SIGNALS.getLength();
		if(debug)System.out.println("numEnemies = " + numEnemies);
		float vpModifier = (rc.getOpponentVictoryPoints() > 500 && rc.getTeamVictoryPoints() - rc.getOpponentVictoryPoints() < 50 ? 2 : 0);
		float distressModifier = (float) (1 - numDistressSignals * .5 - numEnemies * .03 > -1 ? 1 - numDistressSignals * .5 - numEnemies * .03 : -1);
		if(area != 0){
			treeToSoldierRatio = (float) (vpModifier + distressModifier + area / 3000); 
		}
		else{
			treeToSoldierRatio = (float) (vpModifier + distressModifier + (roundNum < 200 ? (120 - rushHeuristic)/(float)(50.0) :  conflictDist / 40)); 
		}
		if(debug)System.out.println("tree to soldier ratio = " + treeToSoldierRatio);
		if(treesToClear == 0 && Message.GENETICS.getValue() == CLEAR_TREES){
			Message.GENETICS.setValue(BUILD_TREES);
		}
		else if(treesToClear > 0 && Message.GENETICS.getValue() == BUILD_TREES){
			Message.GENETICS.setValue(CLEAR_TREES);
		}
		if(numDistressSignals > 0){
			//rc.setIndicatorLine(here, Message.DISTRESS_SIGNALS.getClosestLocation(here), 255, 0, 0);
			lastTurnWithDistress = roundNum;
			Message.ADAPTATION.setValue(DEFEND_SOMETHING);
		}
		else if (roundNum - lastTurnWithDistress > 0){
			Message.ADAPTATION.setValue(DEFEND_NOTHING);
			if(roundNum > 150 && Message.GENETICS.getValue() == RUSH_ENEMY){
				Message.GENETICS.setValue(treesToClear > 0 ? CLEAR_TREES: BUILD_TREES);
			}
		}
		switch(genetics){
		case RUSH_VP:
			if(numGardener < 8){
				Message.ARCHON_BUILD_NUM.setValue(1);
			}
			else{
				Message.ARCHON_BUILD_NUM.setValue(0);
			}
			switch (adaptation) {
			case DEFEND_NOTHING:
				if(rc.getTeamVictoryPoints() > 1000 - rc.getTreeCount() * 8 || rc.getTeamVictoryPoints() - rc.getOpponentVictoryPoints() < 50 && rc.getOpponentVictoryPoints() > 700)
					break;
				if (numSoldier < initialSoldiers) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(initialSoldiers - numSoldier);
				} else if (numSoldier >= initialSoldiers && numSoldier < (int)(rc.getTreeCount() / treeToSoldierRatio) + 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue((int) ((int)(rc.getTreeCount() / treeToSoldierRatio) - numSoldier) + 1);
				}
				break;
			case DEFEND_SOMETHING:
				if(rc.getTeamVictoryPoints() > 1000 - rc.getTreeCount() * 5 || rc.getTeamVictoryPoints() - rc.getOpponentVictoryPoints() < 50 && rc.getOpponentVictoryPoints() > 700)
					break;
				if (numSoldier < initialSoldiers) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(initialSoldiers - numSoldier);
				} else if (numSoldier >= initialSoldiers && numSoldier < (int)(rc.getTreeCount() / treeToSoldierRatio) + 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue((int) ((int)(rc.getTreeCount() / treeToSoldierRatio) - numSoldier) + 3);
				}
				break;
			}
			break;
		case RUSH_ENEMY:
			if (numGardener == 0 ||  
				    rc.getRoundNum() > 100 && (Message.ADAPTATION.getValue() != DEFEND_SOMETHING || rc.getTeamBullets() > 150) && 
				    (double)(rc.getTreeCount()) / numGardener > (rc.getTeamBullets() > roundNum ? 1.5: 2.5)
					&& (numGardener == 1 && numEnemies < initialEnemyArchonLocations.length + 2 || rc.getTeamBullets() > 175)) {
				Message.ARCHON_BUILD_NUM.setValue(1);
			}
			else{
				Message.ARCHON_BUILD_NUM.setValue(0);
			}
			switch (adaptation) {
			case 0:
			case DEFEND_NOTHING:
				if (numSoldier < initialSoldiers ) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(initialSoldiers - numSoldier);
				} else if (numSoldier >= initialSoldiers && numSoldier < (int)(rc.getTreeCount() / treeToSoldierRatio) + 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue((int) (rc.getTreeCount()/treeToSoldierRatio - numSoldier) + 1);
				}
				break;
			case DEFEND_SOMETHING:
				if (numSoldier < initialSoldiers) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(initialSoldiers - numSoldier);
				} else if (numSoldier >= initialSoldiers && numSoldier < (int)(rc.getTreeCount() / treeToSoldierRatio) + 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue((int) (rc.getTreeCount()/treeToSoldierRatio - numSoldier) + 3);
				} 
				break;
			/*not in use
			case CLEAR_AS_WELL:
				if (numSoldier < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(2 - numSoldier);
				} else if (numLumberjack < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(LUMBERJACK);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} else if (numSoldier < 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(3 - numSoldier);
				} else if (numScout < 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SCOUT);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} else if (numSoldier < rc.getTreeCount() && numSoldier < 8) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue((int)(rc.getTreeCount() * 2.0/3.0 - numSoldier));
				} else if (rc.getTreeCount() > 8 && rc.getTeamBullets() < 500 && ((numTank + 1) * 4 < numSoldier)){
					Message.GARDENER_BUILD_ORDERS.setValue(TANK);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} else if (numSoldier > 7 && numSoldier < rc.getTreeCount() && numSoldier < (area > 0 ? area: conflictDist * 15) / 200) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue((int)(rc.getTreeCount() - numSoldier));
				}
				break;*/
			}
			break;
		case CLEAR_TREES:
			if (numGardener == 0 ||  
		    	rc.getRoundNum() > 100 && (Message.ADAPTATION.getValue() != DEFEND_SOMETHING || rc.getTeamBullets() > 150) && 
		    	(double)(rc.getTreeCount()) / numGardener > (rc.getTeamBullets() > roundNum ? 1.5: 2.5) 
				&& (numGardener == 1 && numEnemies < initialEnemyArchonLocations.length + 2 || rc.getTeamBullets() > 175)) {
				Message.ARCHON_BUILD_NUM.setValue(1);
			}
			else{
				Message.ARCHON_BUILD_NUM.setValue(0);
			}
			switch (adaptation) {
			case 0:
			case DEFEND_NOTHING:
				if (numSoldier < initialSoldiers) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(initialSoldiers - numSoldier);
				} else if (numScout < 1 && shouldBuildScout) {
					Message.GARDENER_BUILD_ORDERS.setValue(SCOUT);
					Message.GARDENER_BUILD_NUM.setValue(1);
				}
				else if ((numLumberjack * 2 < treesToClear + numUnitTrees / 5) && numLumberjack < rc.getTreeCount() && rc.getTreeCount() > 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(LUMBERJACK);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} else if (numSoldier >= initialSoldiers && numSoldier < (int)(rc.getTreeCount() / treeToSoldierRatio) + 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue((int) ((int)(rc.getTreeCount() / treeToSoldierRatio) - numSoldier) + 1);
				}
				break;
			case DEFEND_SOMETHING:
				if (numSoldier < initialSoldiers) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(initialSoldiers - numSoldier);
				} else if (numSoldier >= initialSoldiers && numSoldier < (int)(rc.getTreeCount() / treeToSoldierRatio) + 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue((int) ((int)(rc.getTreeCount() / treeToSoldierRatio) - numSoldier) + 3);
				}
				break;
			}
			break;
		case BUILD_TREES:
			if (numGardener == 0 ||  
	    	rc.getRoundNum() > 100 && (Message.ADAPTATION.getValue() != DEFEND_SOMETHING || rc.getTeamBullets() > 150) && 
	    	(double)(rc.getTreeCount()) / numGardener > (rc.getTeamBullets() > roundNum ? 1.5: 2.5)
			&& (numGardener == 1 && numEnemies < initialEnemyArchonLocations.length + 2 || rc.getTeamBullets() > 175)) {
				Message.ARCHON_BUILD_NUM.setValue(1);
			}
			else{
				Message.ARCHON_BUILD_NUM.setValue(0);
			}
			switch (adaptation) {
			case 0:
			case DEFEND_NOTHING:
				if (numSoldier < initialSoldiers) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(initialSoldiers - numSoldier);
				} else if (numScout < 1 && shouldBuildScout) {
					Message.GARDENER_BUILD_ORDERS.setValue(SCOUT);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} else if ((numLumberjack * 2 < treesToClear + numUnitTrees / 5) && numLumberjack < rc.getTreeCount() && rc.getTreeCount() > 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(LUMBERJACK);
					Message.GARDENER_BUILD_NUM.setValue(1);
				} else if (numSoldier >= initialSoldiers && numSoldier < (int)(rc.getTreeCount() / treeToSoldierRatio) + 1) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM
							.setValue((int) ((int)(rc.getTreeCount() / treeToSoldierRatio) - numSoldier) + 1);
				}
				break;
			case DEFEND_SOMETHING:
				if (numSoldier < initialSoldiers) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue(initialSoldiers - numSoldier);
				} else if (numSoldier >= initialSoldiers && numSoldier < (int)(rc.getTreeCount() / treeToSoldierRatio) + 3) {
					Message.GARDENER_BUILD_ORDERS.setValue(SOLDIER);
					Message.GARDENER_BUILD_NUM.setValue((int) ((int)(rc.getTreeCount() / treeToSoldierRatio) - numSoldier) + 3);
				}
				break;
			}
			break;
		}

	}

}