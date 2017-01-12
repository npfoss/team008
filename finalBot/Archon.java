package team008.finalBot;
import battlecode.common.*;

public class Archon extends Bot {
	public static Direction lastDirection = new Direction(0);
	public static int numGardenersCreated = 0;

	public Archon(RobotController r){
		super(r);
		//anything else archon specific
	}

	public static Direction findOpenSpaces(){
		int spaces = 0;
		Direction dir = new Direction(0);
		float xavg = 0;
		float yavg = 0;
		for(int i =0; i < 36; i++){
			if (rc.canMove(dir, type.sensorRadius)){
				MapLocation temp = here.add(dir, type.sensorRadius);
				xavg+= temp.x;
				yavg+= temp.y;
				spaces++;
			}
			dir = dir.rotateLeftDegrees(10);
		}
		return here.directionTo(new MapLocation(xavg/spaces, yavg/spaces));
		
	}

	public void takeTurn(TreeInfo[] nearbyNeutralTrees) throws Exception{

		if(rc.getRoundNum() % 10==0){
	    lastDirection = findOpenSpaces();
		}
	    if(rc.getRoundNum() + 5 > GameConstants.GAME_DEFAULT_ROUNDS || rc.getTeamVictoryPoints() + rc.getTeamBullets()/10 > 1000){
			rc.donate(((int)(rc.getTeamBullets()/10))*10);
		}
	    else if(rc.getTreeCount() < 20 && rc.getTeamBullets() > 100  && rc.getRoundNum() > 500|| rc.getTeamBullets() > 120 || rc.getRoundNum() < 400 && rc.getTeamBullets() > 100  && Messaging.getStrategy() == 0 || rc.getRoundNum() < 100&& rc.getTeamBullets() > 100){
	    	hireGardener();
		}


	    RobotInfo[] enemies = rc.senseNearbyRobots(-1,enemy);
		RobotInfo[] allies = rc.senseNearbyRobots(-1,us);

	    if(enemies.length > 0){
	    	Messaging.setStrategy(1);
			rc.setIndicatorDot(here,0,255,0);
			runAway(enemies ,allies);
	    }
	    tryMoveDirection(lastDirection);
	}
	

	public void hireGardener() throws GameActionException{
		Direction dir = lastDirection.opposite();
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

	private static double wallModCalc(MapLocation retreatLoc,Direction dir) throws GameActionException{
		double mod = 0;
		while(here.distanceTo(retreatLoc)<type.sensorRadius && rc.onTheMap(retreatLoc)){
			retreatLoc = retreatLoc.add(dir);
			mod+=1.0;

		}
		return mod;

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
			double wallMod = wallModCalc(retreatLoc,dir);

			if (dist+allyMod+wallMod> bestValue) {
				bestValue = dist+allyMod+wallMod;
				bestRetreatDir = dir;
			}
			count++;
			dir = dir.rotateRightDegrees(10);

		}


		if (bestRetreatDir != null) {
			tryMoveDirection(bestRetreatDir);
		}
		tryMoveDirection(Util.randomDirection());
	}
}