package team008.aaron_shit_poop;

import battlecode.common.*;

public class Lumberjack extends Bot {

	public Lumberjack(RobotController r){
		super(r);
		//anything else lumberjack specific
	}
	
	public void takeTurn() throws Exception{
        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

        if(robots.length > 0 && !rc.hasAttacked()) {
            // Use strike() to hit all nearby robots!
            rc.strike();
        } else {
            // No close robots, so search for robots within sight radius
            robots = rc.senseNearbyRobots(-1,enemy);

            // If there is a robot, move towards it
            if(robots.length > 0) {
                MapLocation myLocation = rc.getLocation();
                MapLocation enemyLocation = robots[0].getLocation();
                Direction toEnemy = myLocation.directionTo(enemyLocation);

                tryMove(toEnemy);
            } else {
                // Move Randomly
                tryMove(randomDirection());
            }
        }
	}
}