package team008.finalBot;

import battlecode.common.*;

public class Lumberjack extends Bot {

    float DAMAGE_THEM_MOD;
    float TREE_DAMAGE_MOD;

    public Lumberjack(RobotController r) throws GameActionException{
        super(r);
        myRandomDirection = Util.randomDirection();
//        debug = true;
        DAMAGE_THEM_MOD = 2f;
        TREE_DAMAGE_MOD = .2f;
    }

    public static boolean attacked = false;
    public static boolean moved = false;
    public static Direction myRandomDirection;
    public TreeInfo closestNeutralWithUnit;
    public Message[] messagesToTry = {Message.DISTRESS_SIGNALS, Message.TREES_WITH_UNITS, Message.CLEAR_TREES_PLEASE, Message.ENEMY_TREES, Message.ENEMY_ARCHONS};
    public int[] howFarToGoForMessage = {     30,                       25,                       25,                         20,                  20};
//    public boolean[] checkEnemiesToRemove = { true,                     false,                    false,               true};

    public void takeTurn() throws Exception{
        attacked = false;
        moved = false;
        if(rc.getRoundNum() % 23 == 0){
            myRandomDirection = Util.randomDirection();
        }

        closestNeutralWithUnit = Util.closestTree(nearbyNeutralTrees, rc.getLocation(), true, 50, true);
        if(debug && closestNeutralWithUnit != null) rc.setIndicatorLine(here, closestNeutralWithUnit.getLocation(), 0, 0, 255);

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
            if(rc.canSenseLocation(target) && rc.isLocationOccupiedByTree(target) && rc.canChop(target))
                bugState = BugState.DIRECT;
            goTo(target);
            moved = true;
        }

//        int start = Clock.getBytecodeNum();
        goForTrees(); // moves towards them and chops
//        if(debug) System.out.println(" going for trees took " + (Clock.getBytecodeNum() - start));

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
        target = (closestNeutralWithUnit == null ? null : closestNeutralWithUnit.getLocation());
        MapLocation targetD;

        for(int i = 0; i < messagesToTry.length; i++){
            //System.out.println("loops " + i);
            targetD = messagesToTry[i].getClosestLocation(here);
            if (targetD != null && here.distanceTo(targetD) < howFarToGoForMessage[i]*howDesperate && (target == null || (here.distanceTo(targetD) < here.distanceTo(target) && here.distanceTo(targetD) < 7))) {
                //if(debug)System.out.println("targetD = " + targetD);
                target = targetD;
            }
        }

        if (target != null && rc.getLocation().distanceTo(target) < 5){
            //if(debug)System.out.println("thinking about removing");
            if( nearbyEnemyRobots.length == 0 &&
                    Message.ENEMY_ARCHONS.removeLocation(target))
                target = null;
            else if (nearbyEnemyTrees.length == 0 &&
                    Message.ENEMY_TREES.removeLocation(target))
                target = null;
            else if (here.distanceTo(target) < 1.5 &&
                    Message.TREES_WITH_UNITS.removeLocation(target))
                target = null;
            else if (nearbyEnemyRobots.length == 0 &&
                    Message.DISTRESS_SIGNALS.removeLocation(target))
                target = null;
            else if (here.distanceTo(target) < 1.5 && (nearbyNeutralTrees.length == 0 || target.distanceTo(nearbyNeutralTrees[0].getLocation()) > 2 )&&
                    Message.CLEAR_TREES_PLEASE.removeLocation(target))
                target = null;
        }
    }

    public void goForTrees() throws GameActionException {
//        int s = Clock.getBytecodeNum();
//        System.out.println("getting closest " + nearbyNeutralTrees.length + " neutral took " + (Clock.getBytecodeNum() - s));
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
                tryMoveDirection(here.directionTo(nearbyEnemyTrees[0].location), false, false);
                if(here.distanceTo(nearbyEnemyTrees[0].getLocation()) > here.add(calculatedMove).distanceTo(nearbyEnemyTrees[0].getLocation())){
                    rc.move(calculatedMove, type.strideRadius);
                }
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
                tryMoveDirection(here.directionTo(nearbyNeutralTrees[0].location), false, false);
                if(here.distanceTo(nearbyNeutralTrees[0].getLocation()) > here.add(calculatedMove).distanceTo(nearbyNeutralTrees[0].getLocation())){
                    rc.move(calculatedMove, type.strideRadius);
                }
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
        if(debug) rc.setIndicatorDot(here, 255, 0 , 0);
        calculatedMove = null;
        tryMoveDirection(here.directionTo(nearbyEnemyRobots[0].location), false, true);
        if(calculatedMove != null && here.add(calculatedMove, type.strideRadius).distanceTo(nearbyEnemyRobots[0].location) < here.distanceTo(nearbyEnemyRobots[0].location)){
            rc.move(calculatedMove, type.strideRadius);
            moved = true;
        }
        if(evalForAttacking(rc.getLocation()) > 0) {
            rc.strike();
            attacked = true;
        }
        if(calculatedMove != null && !moved){
            rc.move(calculatedMove, type.strideRadius);
            moved = true;
        }
        if(!attacked && evalForAttacking(rc.getLocation()) > 0) {
            rc.strike();
            attacked = true;
        }
    }

   /* public void doLumberjackMicro() throws Exception{
        // gets called when there are enemies that can be seen
        // don't worry about chopping trees here, that's checked for after. only enemies
        if (debug) { System.out.println("whee micro"); }

        // TODO: add kamikaze function: if about to die anyways, just go for best place to attack for final stand

        float attackScoreHere = evalForAttacking(here);
        float bestMoveScore = evaluateLocation(here) + (attackScoreHere < 0 ? 0 : MOVE_ATTACK_MOD * attackScoreHere) + IMPATIENCE_MOD * turnsWithoutMovingOrAttacking;
        // there should be a way to remove the attack bit here such that
        //     a better location won't be disregarded since we can attack
        //     here and then move there
        if (debug) { System.out.println("here score " + bestMoveScore); }
        MapLocation bestLoc = here;
        float bestLocAttackScore = -999;
        MapLocation currLoc;
        float score, attackScore;
        int startTheta = 0; // if we want to start at something nonzero then change the hardcoded zeroes below
        int currentTheta = 0;
        int dtheta = 36;
        int numLocsEvaled = 0;
        float stridedist = RobotType.LUMBERJACK.strideRadius;

        int startBytecode = Clock.getBytecodeNum();
        while (Clock.getBytecodeNum() + (Clock.getBytecodeNum() - startBytecode)/((numLocsEvaled < 2 ? 1 : numLocsEvaled)) < WHEN_TO_STOP_MICRO){
            // stop when the average time it takes to eval puts us over the WHEN_TO_STOP_MICRO threshold
            currLoc = here.add(Util.radians(currentTheta), stridedist);
            if (rc.canMove(currLoc)) {
                if (debug) { rc.setIndicatorDot(currLoc, 0, 0, (int)(1.0*currentTheta / 360 * 255)); }
                attackScore = evalForAttacking(currLoc);
                score = evaluateLocation(currLoc) + (attackScoreHere < 0 ? 0 : MOVE_ATTACK_MOD * attackScoreHere);
                //                                  if you're not going to attack anyways, it doesn't matter how bad it is
                if (debug) { System.out.println(currentTheta + " " + currLoc.x + " " + currLoc.y + " score " + score); }
                if (score > bestMoveScore) {
                    bestLoc = currLoc;
                    bestLocAttackScore = attackScore;
                    bestMoveScore = score;
                }
                numLocsEvaled += 1;
            }

            currentTheta += dtheta;
            if (currentTheta >= 360){
                // tried every point around a circle, now try closer
                // TODO: make the test points more evenly distributed inside (see circle-packing on wikipedia)
                stridedist /= 2;
                currentTheta = 0;
                dtheta = dtheta * 5 / 3; // it'll get more dense as it gets closer so adjust a little for that
                if (stridedist < .187) { // probably silly to keep checking
                    if (debug) { System.out.print("I've tried everything dammit"); }
                    break;
                }
            }
        }
        if (debug) { System.out.println("tried " + numLocsEvaled + " locs and finished at theta " + currentTheta + " and radius " + stridedist); }

        if ( attackScoreHere > bestLocAttackScore && attackScoreHere > 0){
            // attack first, then move
            if (debug) {rc.setIndicatorDot(here, 255,0,0); }//red dot == SMASH
            rc.strike();
            rc.move(bestLoc);

        } else if (bestLocAttackScore > 0){
            // move first, then attack
            rc.move(bestLoc);
            if (debug) { rc.setIndicatorDot(bestLoc, 255, 0, 0); }//red dot == SMASH
            rc.strike();
        } else if (bestLoc != here){
            // just move
            rc.move(bestLoc);
        }
    }

    public float evaluateLocation(MapLocation loc){
        // 'scores' the location in terms of possible damage accrued (bullets and otherwise) and forward progress,
        //     but NOT attacking damage
        // TODO: take into account other strategery like defending our trees/units, swarming or not, etc

        float distToNearestEnemy = (loc == here ? here.distanceTo(nearbyEnemyRobots[0].getLocation()) : Util.distToClosestBody(nearbyEnemyRobots, loc));
        /*if (distToNearestEnemy < GameConstants.LUMBERJACK_STRIKE_RADIUS + RobotType.LUMBERJACK.strideRadius + 1){
            distToNearestEnemy = 0; // close enough to hit already
        }*//*

        return KNOWN_DAMAGE_MOD * knownDamageToLoc(loc)
                + HYPOTHETICAL_DAMAGE_MOD * hypotheticalDamageToSpot(loc)
                + PROXIMITY_MOD * distToNearestEnemy
                + (target != null ? PROGRESS_MOD * here.distanceTo(target) - loc.distanceTo(target) : 0)
                + (gardenerLoc != null ? GARDENER_PROXIMITY_MOD * (loc.distanceTo(gardenerLoc) < 3.6 ? 3.6f - loc.distanceTo(gardenerLoc) : 0) : 0 )
                // translation: if too close to gardener I'm defending, it's bad. (3.6 isn't random, it's sqrt(3)*3/2)
                ;
    }*/

    public float evalForAttacking(MapLocation loc){
        // how good it is to attack from this spot
        // score <= 0 means it's better not to attack
        float damageToThem = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyEnemyRobots, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        float damageToUs = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyAlliedRobots, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        float damageToEnemyTrees = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyEnemyTrees, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        float damageToAlliedTrees = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyAlliedTrees, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        return DAMAGE_THEM_MOD * damageToThem - damageToUs + TREE_DAMAGE_MOD * (damageToEnemyTrees - damageToAlliedTrees);
    }
}