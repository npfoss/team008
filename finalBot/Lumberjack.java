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

        scanForGardenerInDistress(); //This needs to be looked over
        
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
        if(target != null){
        	rc.setIndicatorLine(here, target, 255, 0, 0);
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


    public void scanForGardenerInDistress()throws GameActionException {
        MapLocation targetD = Messaging.getClosestDistressSignal(here);
        if (targetD != null && target == null && here.distanceTo(targetD) < 50) {
            target = targetD;
        }

        if (target != null && rc.getLocation().distanceTo(target) < 6 && nearbyEnemyRobots.length == 0){
            Messaging.removeDistressLocation(target);
            target = null;
        }
    }




}