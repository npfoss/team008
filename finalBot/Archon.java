package team008.finalBot;

import battlecode.common.*;


public class Archon extends Bot {

	public static int numGardenersCreated = 0;
	private static Direction lastRetreatDir;
	private static int lastTurnFled;


	public Archon(RobotController r){
		super(r);
		//anything else archon specific
	}
	
	public void takeTurn() throws Exception{

	    // Generate a random direction
	    if(rc.getRoundNum() + 5 > GameConstants.GAME_DEFAULT_ROUNDS || rc.getTeamVictoryPoints() + rc.getTeamBullets()/10 > 1000){
			rc.donate(((int)(rc.getTeamBullets()/10))*10);
		}
	    else if(rc.getTeamBullets() > 150 || rc.getTreeCount() > numGardenersCreated * (3*GameConstants.NUMBER_OF_ARCHONS_MAX) || rc.getRoundNum() < 400 && rc.getTeamBullets() > 100){
	    	hireGardener();
		}


	    RobotInfo[] enemies = rc.senseNearbyRobots(-1,enemy);
		RobotInfo[] allies = rc.senseNearbyRobots(-1,us);
		runAway(enemies ,allies);
	}
	

	public void hireGardener() throws GameActionException{
		Direction dir = Util.randomDirection();
		for(int i = 15; i --> 0;){
		    if (rc.canHireGardener(dir)) {
		        rc.hireGardener(dir);
		        numGardenersCreated++;
		        break;
		    }
		    else{
		    	dir = dir.rotateLeftDegrees(24);
		    }
		}
	}

	public void runAway(RobotInfo[] enemies, RobotInfo[] allies) throws GameActionException{
		Direction bestRetreatDir = null;
		double bestValue = -10000;
		int count = 0;
		Direction dir = new Direction(0);

		while( count < 36 ) {

			MapLocation retreatLoc = here.add(dir,rc.getType().strideRadius);
			RobotInfo closestEnemy = Util.closestRobot(enemies, retreatLoc);

			float dist = retreatLoc.distanceTo(closestEnemy.location);
			double allyMod = RangedCombat.numOtherAlliesInSightRange( here.add(dir,rc.getType().strideRadius), allies);

			if (dist+allyMod > bestValue) {
				bestValue = dist+allyMod;
				bestRetreatDir = dir;
			}
			count++;
			Direction right = dir.rotateRightDegrees(10);

		}


		if (bestRetreatDir != null) {
			tryMoveDirection(bestRetreatDir);
		}
		tryMoveDirection(Util.randomDirection());
	}
}