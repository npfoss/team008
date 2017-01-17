package team008.finalBot;
import battlecode.common.*;


public class Soldier extends Bot {

    public Soldier(RobotController r) throws GameActionException{
        super(r);
        //anything else soldier specific
    }
    
	public void takeTurn() throws Exception{

//		if(target != null){
//			rc.setIndicatorDot(target, 255, 0, 0);
//		}

		if(nearbyEnemyRobots.length > 0){
			if((rc.getRoundNum() +rc.getID()) % 25 == 0 || target == null){
				notifyFriendsOfEnemies(nearbyEnemyRobots);
			}
			RangedCombat.execute();
			return;
		}
		else if(target == null){
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