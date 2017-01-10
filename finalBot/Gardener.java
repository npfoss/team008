package team008.finalBot;

import battlecode.common.*;

public class Gardener extends Bot {
	
	static int numTreesBuilt = 0;
	static int numLumberjacksBuilt = 0;
	static int numScoutsBuilt = 0;
	static int numSoldiersBuilt = 0;
	static int numTanksBuilt = 0;

	public Gardener(RobotController r){
		super(r);
		//anything else gardener specific
	}
	
	public void takeTurn() throws Exception{
		// Listen for home archon's location
		/*
        int xPos = rc.readBroadcast(0);
        int yPos = rc.readBroadcast(1);
        MapLocation archonLoc = new MapLocation(xPos,yPos);*/
		
		buildSomething();

        // Move toward lowest health tree
		TreeInfo[] trees = rc.senseNearbyTrees();
		TreeInfo treeToHeal = Util.leastHealth(trees);
		if(treeToHeal != null){
			tryMove(rc.getLocation().directionTo(treeToHeal.location));
		}
        
        waterLowestHealthTree();
	}
	
	public void waterLowestHealthTree() throws GameActionException {
		TreeInfo[] treesToWater = rc.senseNearbyTrees(1);
		TreeInfo treeToHeal = Util.leastHealth(treesToWater);
		if(treeToHeal != null){
			rc.water(treeToHeal.location);
		}
	}

	public void buildSomething() throws GameActionException {
		if(numTreesBuilt < 2){
			if(rc.getTeamBullets() >= GameConstants.BULLET_TREE_COST){
				plantATree();
				numTreesBuilt++;
			}
		}
		else if (numScoutsBuilt < 1){
			if(rc.getTeamBullets() >= 80){
				numScoutsBuilt++;
				buildRobot(RobotType.SCOUT);
			}
		}
		else if (numLumberjacksBuilt < 1){
			if(rc.getTeamBullets() >= 100){
				numLumberjacksBuilt++;
				buildRobot(RobotType.LUMBERJACK);
			}
		}
		else if(numTreesBuilt < 4){
			if(rc.getTeamBullets() >= GameConstants.BULLET_TREE_COST){
				plantATree();
				numTreesBuilt++;
			}
		}
		else {
			if(rc.getTeamBullets() >= 100){
				numSoldiersBuilt++;
				buildRobot(RobotType.SOLDIER);
			}
		}
		
	}

	public void buildRobot(RobotType type) throws GameActionException{
		for(int i = 15; i --> 0;){
			Direction dir = randomDirection();
		    if (rc.canBuildRobot(type, dir)) {
		        rc.buildRobot(type, dir);
		        break;
		    }
		}
	}
	
	public void plantATree() throws GameActionException{
		for(int i = 15; i --> 0;){
			Direction dir = randomDirection();
		    if (rc.canPlantTree(dir)) {
		        rc.plantTree(dir);
		        break;
		    }
		}
	}
}