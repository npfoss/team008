package team008.finalBot;

import battlecode.common.*;

public class Soldier extends Bot {

    public Soldier(RobotController r){
        super(r);
        //anything else soldier specific
    }
    
	public void takeTurn() throws Exception{
        MapLocation myLocation = rc.getLocation();

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
            }
        }

        // Move randomly
        tryMove(randomDirection());
    }
}