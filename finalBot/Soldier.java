package team008.finalBot;
import battlecode.common.*;


public class Soldier extends Bot {
	private static boolean isDefender;
	private static MapLocation gardenerLoc;

    public Soldier(RobotController r) throws GameActionException{
        super(r);
        if(rc.readBroadcast(23) == 1){
	        RobotInfo gardener = Util.closestSpecificType(rc.senseNearbyRobots(-1, us),rc.getLocation(),RobotType.GARDENER);
	        //System.out.println("hello");
	        if(gardener != null){
		        gardenerLoc = gardener.location;
	        	isDefender = true;
	        	rc.broadcast(23, 0);
	        }
        }
        else{
        	isDefender = false;
        }
        //anything else soldier specific
    }
    
	public void takeTurn() throws Exception{

//		if(target != null){
//			rc.setIndicatorDot(target, 255, 0, 0);
//		}
		if(isDefender){
			System.out.println("I am a defender");
			if(rc.canSenseLocation(gardenerLoc) && rc.senseRobotAtLocation(gardenerLoc) == null){
				isDefender = false;
			}
			else if(nearbyEnemyRobots.length > 0){
				if(rc.canSenseLocation(gardenerLoc)){
					DefenseMicro.defend(rc.senseRobotAtLocation(gardenerLoc));
				}
				else{
					RangedCombat.execute();
				}
			}
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

	private void circleGardener(MapLocation gLoc) throws GameActionException {
		if(here.distanceTo(gLoc) > 4.5 || here.distanceTo(gLoc) < 3.5){
			goTo(gLoc.add(gLoc.directionTo(here), 4));
		}
		else{
			Direction dir = gLoc.directionTo(here);
			goTo(gLoc.add(dir.rotateLeftDegrees(36), 4));
		}
	}
	
}