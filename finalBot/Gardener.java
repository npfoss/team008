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
	
	public void takeTurn(TreeInfo[] nearbyNeutralTrees) throws GameActionException{
		//
		RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemy);
	    if(enemyRobots.length > 0){
	    	Messaging.setStrategy(1);
	    }
	    rc.setIndicatorDot(here, 0, 0, 255);
//		// Listen for home archon's location
//        int xPos = rc.readBroadcast(0);
//        int yPos = rc.readBroadcast(1);
//        MapLocation archonLoc = new MapLocation(xPos,yPos);
//
//        // Generate a random direction
//        Direction dir = randomDirection();
//
//        // Randomly attempt to build a soldier or lumberjack in this direction
//        if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
//            rc.buildRobot(RobotType.SOLDIER, dir);
//        } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
//            rc.buildRobot(RobotType.LUMBERJACK, dir);
//        }
//
//        // Move randomly
//        tryMove(randomDirection());
		waterLowestHealthTree();
		//rc.setIndicatorDot(here, 0, 0, 255);
		buildSomething();
	}
	public void waterLowestHealthTree() throws GameActionException {
		TreeInfo[] treesToWater = rc.senseNearbyTrees(-1,us);
		TreeInfo treeToHeal = Util.leastHealth(treesToWater, true);
		if(treeToHeal != null){
			rc.water(treeToHeal.getID());
		}
	}
	public void buildSomething() throws GameActionException {

//		if(numTreesBuilt < 2){
//			if(rc.getTeamBullets() >= GameConstants.BULLET_TREE_COST){
//				plantATree();
//			}
//		}
//		else if (numScoutsBuilt < 1){
//			if(rc.getTeamBullets() >= 80){
//				numScoutsBuilt++;
//				buildRobot(RobotType.SCOUT);
//			}
//		}
//		else if (numLumberjacksBuilt < 1){
//			if(rc.getTeamBullets() >= 100){
//				numLumberjacksBuilt++;
//				buildRobot(RobotType.LUMBERJACK);
//			}
//		}
	//	rc.setIndicatorDot(here, 0, 255, 0);
		if(numScoutsBuilt < 1 && rc.getRoundNum() < 50){
			if(rc.getTeamBullets() > 80){
				//rc.setIndicatorDot(here, 255, 0, 0);
				numScoutsBuilt++;
				buildRobot(RobotType.SCOUT);
			}
		}
		else if(numTreesBuilt < 5 && (Messaging.getStrategy() == 0 || rc.getRoundNum()>400)){
			if(rc.getTeamBullets() >= GameConstants.BULLET_TREE_COST){
				plantATree();
			}
		}
		if(rc.getTeamBullets() > 100){
			if(rc.getRoundNum() > 300){	
				numSoldiersBuilt++;
				buildRobot(RobotType.SOLDIER);
			}
			else{
				numLumberjacksBuilt++;
				buildRobot(RobotType.LUMBERJACK);
			}
		}	
	}

	public void buildRobot(RobotType type) throws GameActionException{

		Direction dir = new Direction(0);
		for(int i = 36; i --> 0;){
		    if (rc.canBuildRobot(type, dir)) {
		        rc.buildRobot(type, dir);
		        break;
		    }
		    else{
		    	dir = dir.rotateLeftDegrees(10);
		    }
		}
	}
	
	public void plantATree() throws GameActionException{

		Direction dir = new Direction(0);
		Boolean skipped = false;
		for(int i = 35; i --> 0;){
		    if (rc.canPlantTree(dir)) {
		        if (skipped){
		    	rc.plantTree(dir);
		    	numTreesBuilt++;
		        break;
		        }
		        else{
		        	skipped = true;
		        }
		    }
		    if(skipped){
		    	dir = dir.rotateLeftDegrees(60);
		    	i-=5;
		    	}
		    	else{
		    		dir = dir.rotateLeftDegrees(10);
		    	}
		    }
		}
}