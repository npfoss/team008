package team008.finalBot;
import battlecode.common.*;

public class Tank extends Bot {

	public Tank(RobotController r) throws GameActionException{
		super(r);
	}

	public void takeTurn() throws Exception{
		RobotInfo[] enemies = rc.senseNearbyRobots(-1,enemy);
		if(enemies.length > 0){
			if(rc.getRoundNum() % 35 == 0){
				Util.notifyFriendsOfEnemies(enemies);
			}
			RangedCombat.execute();
			return;
		}
		if(target == null){
			assignNewTarget();
		}
		else if (target != null && rc.getLocation().distanceTo(target) < 2 && enemies.length == 0){
			Messaging.removeEnemyArmyLocation(target);
			Messaging.removeEnemyUnitLocation(target);
			target = null;
			assignNewTarget();
		}
		if(target != null){
			goTo(target);
		}
		else{
			goTo(here.directionTo(Util.rc.getInitialArchonLocations(enemy)[0]));
		}
	}
}