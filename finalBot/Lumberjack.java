package team008.finalBot;

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

            //Notify allies of enemies
            if((rc.getRoundNum() +rc.getID()) % 25 == 0 || target == null){
                notifyFriendsOfEnemies(nearbyEnemyRobots);
            }
            moved = true;
            if(here.distanceTo(nearbyEnemyRobots[0].location) <= GameConstants.LUMBERJACK_STRIKE_RADIUS + nearbyEnemyRobots[0].type.bodyRadius){
                rc.strike();
                attacked = true;
            }
        }
        else{
        	updateTarget(); 
        }
        if(target != null){
        	if(debug) { rc.setIndicatorLine(here, target, (us == Team.A ? 255: 0), (us == Team.A ? 0: 255), 0); };
            goTo(target);
            moved = true;
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


    public void updateTarget()throws GameActionException {
    	if(target != null && roundNum + rc.getID() / 10 == 0 && !Message.DISTRESS_SIGNALS.containsLocation(target) && !Message.ENEMY_ARCHONS.containsLocation(target) ){
    		if(debug)System.out.println("changing");
    		target = null;
    	}
        MapLocation targetD = Message.DISTRESS_SIGNALS.getClosestLocation(here);
        if (targetD != null && target == null && here.distanceTo(targetD) < 25) {
        	//if(debug)System.out.println("targetD = " + targetD);
            target = targetD;
        }
        if(target ==  null){
            MapLocation targetA = Message.ENEMY_ARCHONS.getClosestLocation(here);
            if(targetA != null && here.distanceTo(targetA) < 25){
            	//if(debug)System.out.println("targetA = " + targetA);
            	target = targetA;
            }
        }
        if (target != null && rc.getLocation().distanceTo(target) < 6 && nearbyEnemyRobots.length == 0){
        	if(debug)System.out.println("removing");
            Message.ENEMY_ARCHONS.removeLocation(target);
            Message.DISTRESS_SIGNALS.removeLocation(target);
            target = null;
        }
    }




}