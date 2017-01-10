package team008.finalBot;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Archon extends Bot {

	public Archon(RobotController r){
		super(r);
		//anything else archon specific
	}
	
	public void takeTurn() throws Exception{
	    // Generate a random direction
	    Direction dir = randomDirection();

	    // Randomly attempt to build a gardener in this direction
	    /*if (rc.canHireGardener(dir) && Math.random() < .01 && false) {
	        rc.hireGardener(dir);
	    }*/

	    // Move randomly
	    tryMove(randomDirection());

	    // Broadcast archon's location for other robots on the team to know
	    MapLocation myLocation = rc.getLocation();
	    rc.broadcast(0,(int)myLocation.x);
	    rc.broadcast(1,(int)myLocation.y);
	    rc.donate(((int)(rc.getTeamBullets()/10))*10);
	}
}