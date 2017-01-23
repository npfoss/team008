package team008.finalBot;
import battlecode.common.*;


public class Soldier extends Bot {

    public Soldier(RobotController r) throws GameActionException{
        super(r);
    }
    
	public void takeTurn() throws Exception{
        //if(debug)System.out.println("In instantiation:"+Clock.getBytecodeNum());
        if(nearbyEnemyRobots.length > 0 && !(nearbyEnemyRobots.length == 1 && nearbyEnemyRobots[0].type == RobotType.ARCHON)){
            if((rc.getRoundNum() +rc.getID()) % 25 == 0 || target == null){
                notifyFriendsOfEnemies(nearbyEnemyRobots);
            }
            RangedCombat.execute();
        }
        if(target == null || (rc.getRoundNum() + rc.getID()) % 10 == 0){
            assignNewTarget();
        }

		if (rc.getMoveCount() == 0) {
			if (target != null && rc.getLocation().distanceTo(target) < 6 && nearbyEnemyRobots.length == 0) {
				Message.ENEMY_ARMIES.removeLocation(target);
				Message.ISOLATED_ENEMIES.removeLocation(target);
				Message.DISTRESS_SIGNALS.removeLocation(target);
				Message.ENEMY_ARCHONS.removeLocation(target);
				assignNewTarget();
			}
			else if (target != null) {
				if (debug) {
		        	if(debug) { rc.setIndicatorLine(here, target, (us == Team.A ? 255: 0), (us == Team.A ? 0: 255), 0); };
				}
				goTo(target);
			} 
			else {
				goTo(here.directionTo(Util.rc.getInitialArchonLocations(enemy)[0]));
			}
		}
	}
	}
