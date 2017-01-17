package team008.finalBot;
import battlecode.common.*;


public class Soldier extends Bot {
	private static boolean isDefender;

    public Soldier(RobotController r) throws GameActionException{
        super(r);
        isDefender = false;
        if(rc.readBroadcast(23) == 1){
	        RobotInfo gardener = Util.closestSpecificType(rc.senseNearbyRobots(-1, us),rc.getLocation(),RobotType.GARDENER);
	        //System.out.println("hello");
	        if(gardener != null){
		        gardenerLoc = gardener.location;
	        	isDefender = true;
	        	rc.broadcast(23, 0);
	        }
        }
        //anything else soldier specific
    }
    
	public void takeTurn() throws Exception{

		if(target != null){
			rc.setIndicatorLine(here,target, 255, 0, 0);
		}
		if(isDefender){
			//System.out.println("I am a defender");
			if(rc.canSenseLocation(gardenerLoc) && rc.senseRobotAtLocation(gardenerLoc) == null){
				isDefender = false;
			}
			else if(nearbyEnemyRobots.length > 0){
				if(rc.canSenseLocation(gardenerLoc)){
					//System.out.println("defending");
					DefenseMicro.defend(rc.senseRobotAtLocation(gardenerLoc));
				}
				else{
					RangedCombat.execute();
				}
			}/*
			else if (target == null){
				MapLocation dis = Messaging.getClosestDistressSignal(here);
				if(dis!= null && here.distanceTo(dis) < 15){
					//System.out.println("going to distress location");
					target = dis;
					goTo(target);
				}
				else{
					circleGardener(gardenerLoc);
				}
			}
			else{
				if(here.distanceTo(target) > 4)
					goTo(target);
				else{
					Messaging.removeDistressLocation(target);
					target = null;
				}
			}*/
			else{
				circleGardener(gardenerLoc);
			}
		}
		else{
			if(nearbyEnemyRobots.length > 0){
				if((rc.getRoundNum() +rc.getID()) % 25 == 0 || target == null){
					notifyFriendsOfEnemies(nearbyEnemyRobots);
				}
				RangedCombat.execute();
				return;
			}
			if(target == null || (rc.getRoundNum() + rc.getID()) % 20 == 0){
				assignNewTarget();
			}
			if (target != null && rc.getLocation().distanceTo(target) < 6 && nearbyEnemyRobots.length == 0){
				Messaging.removeEnemyArmyLocation(target);
				Messaging.removeEnemyUnitLocation(target);
				Messaging.removeDistressLocation(target);
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
	
}