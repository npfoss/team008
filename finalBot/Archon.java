package team008.finalBot;

import battlecode.common.*;
import finalBot01.Util;


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
	public void takeTurn() throws Exception{
	    // Generate a random direction
		if(rc.getRoundNum() % 10==0){
	    lastDirection = findOpenSpaces();
		}
	    if(rc.getRoundNum() + 5 > GameConstants.GAME_DEFAULT_ROUNDS || rc.getTeamVictoryPoints() + rc.getTeamBullets()/10 > 1000){
			rc.donate(((int)(rc.getTeamBullets()/10))*10);
		}
	    else if(rc.getTeamBullets() > 120 || rc.getRoundNum() < 400 && rc.getTeamBullets() > 100  && Messaging.getStrategy() == 0 || rc.getRoundNum() < 100&& rc.getTeamBullets() > 100){
	    	hireGardener();
		}
	    // Randomly attempt to build a gardener in this direction
	    //if (rc.canHireGardener(dir) && Math.random() < .01 && false) {
	    //    rc.hireGardener(dir);
	    //}


	    // Move randomly
//		if(rc.senseBroadcastingRobotLocations().length > 0){
//	    goTo(rc.senseBroadcastingRobotLocations()[0]);
//		}
//		else{

	    RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemy);
	    if(enemyRobots.length > 0){
	    	Messaging.setStrategy(1);
	    	lastDirection = Util.closest(enemyRobots, here).location.directionTo(here);
	    }
	    tryMoveDirection(lastDirection);
//		}
	    // Broadcast archon's location for other robots on the team to know
	    /*
	    MapLocation myLocation = rc.getLocation();
	    rc.broadcast(0,(int)myLocation.x);
	    rc.broadcast(1,(int)myLocation.y);*/
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
}