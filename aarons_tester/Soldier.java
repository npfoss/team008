package team008.aarons_tester;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Soldier extends Bot {

    public Soldier(RobotController r){
        super(r);
        //anything else soldier specific
    }
    
	public void takeTurn() throws Exception{

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        // If there are some...
        

        // Move randomly
        boolean hasMoved = false;
        Direction toMove = randomDirection();
        if(robots.length == 1) {
            MapLocation enemyLocation = robots[0].getLocation();
             toMove = here.directionTo(enemyLocation);

            tryMoveDirection(toMove);
            hasMoved = true;
        } else if (robots.length > 1)
        {
        	MapLocation centroid = Util.centroidOfUnits(robots);
        	 toMove = centroid.directionTo(here);
        	//tryMoveDirection(awayEnemy);
        }
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
            }
        }
        if(!hasMoved){
        	tryMoveDirection(toMove);
        }
    }
}