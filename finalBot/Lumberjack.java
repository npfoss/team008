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
    public Message[] messagesToTry = {Message.DISTRESS_SIGNALS, Message.TREES_WITH_UNITS, Message.CLEAR_TREES_PLEASE, Message.ENEMY_TREES, Message.ENEMY_ARCHONS};
    public int[] howFarToGoForMessage = {     30,                       25,                       25,                         20,                  20};
//    public boolean[] checkEnemiesToRemove = { true,                     false,                    false,               true};

    public void takeTurn() throws Exception{
        attacked = false;
        moved = false;
        if(rc.getRoundNum() % 23 == 0){
            myRandomDirection = Util.randomDirection();
        }

        
        if(nearbyEnemyRobots.length > 0) {
            //Notify allies of enemies
            if((rc.getRoundNum() +rc.getID()) % 25 == 0 || target == null){
                notifyFriendsOfEnemies(nearbyEnemyRobots);
            }

            doMicro();
        } else {
        	updateTarget(1);
        	if(target == null){
        	    updateTarget(2);
            }
        }
        if(target != null && !moved){
            goTo(target);
            moved = true;
        }

        goForTrees(); // moves towards them and chops

        if (!moved) { // no trees around // just random ish
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
        if(debug && target != null) { rc.setIndicatorLine(here, target, (us == Team.A ? 255: 0), (us == Team.A ? 0: 255), 0); };
    }

    public void updateTarget(int howDesperate) throws GameActionException {
    	/*if(target != null && roundNum + rc.getID() % 10 == 0 && !Message.DISTRESS_SIGNALS.containsLocation(target) && !Message.ENEMY_ARCHONS.containsLocation(target) ){
    		//if(debug)System.out.println("changing");
    		target = null;
    	}*/
    	target = null;
        MapLocation targetD;

        for(int i = 0; i < messagesToTry.length && target == null; i++){
            targetD = messagesToTry[i].getClosestLocation(here);
            if (targetD != null && here.distanceTo(targetD) < howFarToGoForMessage[i]*howDesperate) {
                //if(debug)System.out.println("targetD = " + targetD);
                target = targetD;
            }
        }

        if (target != null && rc.getLocation().distanceTo(target) < 5){
        	if(debug)System.out.println("removing");
            if( nearbyEnemyRobots.length == 0 &&
                    Message.ENEMY_ARCHONS.removeLocation(target));
            else if (nearbyEnemyTrees.length == 0 &&
                    Message.ENEMY_TREES.removeLocation(target));
            else if (here.distanceTo(target) < 1.5 &&
                    Message.CLEAR_TREES_PLEASE.removeLocation(target));
            else if (here.distanceTo(target) < 1.5 &&
                    Message.TREES_WITH_UNITS.removeLocation(target));
            else if (nearbyEnemyRobots.length == 0 &&
                    Message.DISTRESS_SIGNALS.removeLocation(target));
            target = null;
        }
    }

    public void goForTrees() throws GameActionException {
        TreeInfo closestNeutralWithUnit = Util.closestTree(nearbyNeutralTrees, rc.getLocation(), true);
        if(closestNeutralWithUnit != null){
            if(!moved){
                tryMoveDirection(here.directionTo(closestNeutralWithUnit.location), true, false);
                moved = true;
            }
            if(rc.canChop(closestNeutralWithUnit.ID)){ // includes check for having attacked already
                rc.chop(closestNeutralWithUnit.ID);
                attacked = true;
            }
        }
        if(nearbyEnemyTrees.length > 0){
            if(!moved){
                tryMoveDirection(here.directionTo(nearbyEnemyTrees[0].location), true, false);
                moved = true;
            }
            if(!attacked){
                if(Util.numBodiesTouchingRadius(nearbyEnemyTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS) > 2
                        && Util.numBodiesTouchingRadius(nearbyAlliedRobots, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS) == 0){
                    rc.strike();
                    attacked = true;
                } else if (rc.canChop(nearbyEnemyTrees[0].location)){
                    rc.chop(nearbyEnemyTrees[0].ID);
                    attacked = true;
                }
            }
        } if(nearbyNeutralTrees.length > 0){
            if(!moved){
                tryMoveDirection(here.directionTo(nearbyNeutralTrees[0].location), true, false);
                moved = true;
            }
            if(!attacked){
                if(Util.numBodiesTouchingRadius(nearbyNeutralTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS) > 2
                        && Util.numBodiesTouchingRadius(nearbyAlliedRobots, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS) == 0){
                    rc.strike();
                    attacked = true;
                } else if (rc.canChop(nearbyNeutralTrees[0].location)) {
                    rc.chop(nearbyNeutralTrees[0].ID);
                    attacked = true;
                }
            }
        }
    }

    public void doMicro() throws GameActionException {
        calculatedMove = null;
        tryMoveDirection(here.directionTo(nearbyEnemyRobots[0].location), false, true);
        if(calculatedMove != null && here.add(calculatedMove, type.strideRadius).distanceTo(nearbyEnemyRobots[0].location) < here.distanceTo(nearbyEnemyRobots[0].location)){
            rc.move(calculatedMove, type.strideRadius);
            moved = true;
        }
        if(here.distanceTo(nearbyEnemyRobots[0].location) <= GameConstants.LUMBERJACK_STRIKE_RADIUS + nearbyEnemyRobots[0].type.bodyRadius){
            rc.strike();
            attacked = true;
        }
        if(calculatedMove != null && !moved){
            rc.move(calculatedMove, type.strideRadius);
            moved = true;
        }
    }


}