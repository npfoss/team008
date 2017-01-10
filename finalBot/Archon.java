package team008.finalBot;

import battlecode.common.*;


public class Archon extends Bot {
	
	static int numGardenersCreated = 0;

	public Archon(RobotController r){
		super(r);
		//anything else archon specific
	}
	
	public void takeTurn() throws Exception{
		if(rc.getRoundNum() + 100 > GameConstants.GAME_DEFAULT_ROUNDS){
			rc.donate(((int)(rc.getTeamBullets()/10))*10);
		}
		else if(rc.getTeamBullets() > 325 || rc.getTreeCount() > numGardenersCreated * (5*GameConstants.NUMBER_OF_ARCHONS_MAX) || rc.getRoundNum() < 100 && rc.getTeamBullets() > 100){
			hireGardener();
		}

	    // Move randomly
	    tryMove(randomDirection());

	    // Broadcast archon's location for other robots on the team to know
	    /*
	    MapLocation myLocation = rc.getLocation();
	    rc.broadcast(0,(int)myLocation.x);
	    rc.broadcast(1,(int)myLocation.y);*/
	}
	
	public void hireGardener() throws GameActionException{
		for(int i = 15; i --> 0;){
			Direction dir = randomDirection();
		    if (rc.canHireGardener(dir)) {
		        rc.hireGardener(dir);
		        numGardenersCreated++;
		        break;
		    }
		}
	}
}