package team008.finalBot;
import battlecode.common.*;


public class Soldier extends Bot {

    public Soldier(RobotController r) throws GameActionException{
        super(r);
        //anything else soldier specific
    }
    
	public void takeTurn() throws Exception{
        System.out.println("In instantiation:"+Clock.getBytecodeNum());
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

        if (rc.getMoveCount() == 0) {
                if (target != null && rc.getLocation().distanceTo(target) < 6 && nearbyEnemyRobots.length == 0){
                Messaging.removeEnemyArmyLocation(target);
                Messaging.removeEnemyUnitLocation(target);
                Messaging.removeDistressLocation(target);
                target = null;
                assignNewTarget();
            }
                if (target != null) {
                	if(debug){rc.setIndicatorLine(here, target, 255, 0, 0);}
                    goTo(target);
                } else {
                    goTo(here.directionTo(Util.rc.getInitialArchonLocations(enemy)[0]));
                }
            }
		}
	}
