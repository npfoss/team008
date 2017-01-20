package team008.defendBot00;

import battlecode.common.*;

public class Lumberjack extends Bot {

	public Lumberjack(RobotController r) throws GameActionException{
		super(r);
		myRandomDirection = Util.randomDirection();
	}
	public static boolean attacked = false;
	public static boolean moved = false;
	public static Direction myRandomDirection;
	public void takeTurn() throws Exception{
		attacked = false;
		moved = false;
		if(rc.getRoundNum() % 23 == 0){
			myRandomDirection = Util.randomDirection();
		}
        if(nearbyEnemyRobots.length > 0) {
        	tryMoveDirection(here.directionTo(nearbyEnemyRobots[0].location), true, true);
        	moved = true;
        	if(here.distanceTo(nearbyEnemyRobots[0].location) <= GameConstants.LUMBERJACK_STRIKE_RADIUS + nearbyEnemyRobots[0].type.bodyRadius){
        		rc.strike();
        		attacked = true;
        	}
        }
        if(nearbyEnemyTrees.length > 0){
        	if(!moved){
        		tryMoveDirection(here.directionTo(nearbyEnemyTrees[0].location), true, false);
        		moved = true;
        	}
        	if(!attacked && rc.canChop(nearbyEnemyTrees[0].location)){
        		rc.chop(nearbyEnemyTrees[0].ID);
        		attacked = true;
        	}
        } if(nearbyNeutralTrees.length > 0){
        	if(!moved){
        		tryMoveDirection(here.directionTo(nearbyNeutralTrees[0].location), true, false);
        		moved = true;
        	}
        	if(!attacked && rc.canChop(nearbyNeutralTrees[0].location)){
        		rc.chop(nearbyNeutralTrees[0].ID);
        		attacked = true;
        	}
        }
		if (!moved) {
			MapLocation[] enemyArchonLocs = rc.getInitialArchonLocations(enemy);
			if ((((roundNum + rc.getID()) / 20) % (enemyArchonLocs.length + 1)) == 0) {
				tryMoveDirection(myRandomDirection, true, true);
			} else {
				tryMoveDirection(
						here.directionTo(
								enemyArchonLocs[(((roundNum + rc.getID()) / 20) % (enemyArchonLocs.length + 1)) -1]),
						true, true);
			}
			moved = true;
        }
        	
}
}