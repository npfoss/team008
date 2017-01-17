package team008.oldBot2;
import battlecode.common.*;

public class Tank extends Bot {

	public Tank(RobotController r) throws GameActionException{
		super(r);
	}

	public void takeTurn() throws Exception{
		if(nearbyEnemyRobots.length > 0){
			if((rc.getRoundNum() +rc.getID() )% 25 == 0|| target == null){
				notifyFriendsOfEnemies(nearbyEnemyRobots);
			}
			RangedCombat.execute();
		}
		if(target == null){
			assignNewTarget();
		}
		else if (target != null && rc.getLocation().distanceTo(target) < 3 && nearbyEnemyRobots.length == 0){
			Messaging.removeEnemyArmyLocation(target);
			Messaging.removeEnemyUnitLocation(target);
			target = null;
			assignNewTarget();
		}
		if (rc.getMoveCount() == 0) {
			if (target != null) {
				goTo(target);
			} else {
				goTo(here.directionTo(Util.rc.getInitialArchonLocations(enemy)[0]));
			}
		}
	}
}