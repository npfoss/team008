package team008.soldierBot00;

import battlecode.common.*;

public class Soldier extends Bot {

	public Soldier(RobotController r) throws GameActionException {
		super(r);
		myRandomDirection = Util.randomDirection();
		// anything else soldier specific
	}

	public static Direction myRandomDirection;
	
	public void takeTurn() throws Exception {
		if(rc.getRoundNum() % 23 == 0){
			myRandomDirection = Util.randomDirection();
		}
        if(nearbyEnemyRobots.length > 0) {
        	RangedCombat.execute();
        }
        else {
			MapLocation[] enemyArchonLocs = rc.getInitialArchonLocations(enemy);
			if ((((roundNum + rc.getID()) / 20) % (enemyArchonLocs.length + 1)) == 0) {
				tryMoveDirection(myRandomDirection, true, true);
			} else {
				tryMoveDirection(
						here.directionTo(
								enemyArchonLocs[(((roundNum + rc.getID()) / 20) % (enemyArchonLocs.length + 1)) -1]),
						true, true);
			}
        }
		return;
	}

}