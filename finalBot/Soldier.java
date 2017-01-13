package team008.finalBot;
import battlecode.common.*;


public class Soldier extends Bot {

    public Soldier(RobotController r){
        super(r);
        //anything else soldier specific
    }
    
	public void takeTurn() throws Exception{

		nearbyEnemyRobots = rc.senseNearbyRobots(-1,enemy);
		if(nearbyEnemyRobots.length > 0){
			if(rc.getRoundNum() % 25 == 0){
				Util.notifyFriendsOfEnemies(nearbyEnemyRobots);
			}
			RangedCombat.execute();
			return;
		}
		if(target == null){
			assignNewTarget();
		}
		else if (target != null && rc.getLocation().distanceTo(target) < 2 && nearbyEnemyRobots.length == 0){
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