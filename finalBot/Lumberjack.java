package team008.finalBot;

import battlecode.common.*;

public class Lumberjack extends Bot {
    public int WHEN_TO_STOP_MICRO;
    public float MOVE_ATTACK_MOD;
    public float TREE_DAMAGE_MOD;
    public float KNOWN_DAMAGE_MOD;
    public float HYPOTHETICAL_DAMAGE_MOD;
    public float PROGRESS_MOD;
    public float PROXIMITY_MOD;
    public float IMPATIENCE_MOD;
    public int turnsWithoutMovingOrAttacking;

	public Lumberjack(RobotController r) throws GameActionException{
		super(r);
		//anything else lumberjack specific
        WHEN_TO_STOP_MICRO = RobotType.LUMBERJACK.bytecodeLimit - 2000; //TODO: don't just guess
        MOVE_ATTACK_MOD = 1; // TODO: actually optimize
        TREE_DAMAGE_MOD = .2f; // TODO: actually optimize
        KNOWN_DAMAGE_MOD = -1;
        HYPOTHETICAL_DAMAGE_MOD = -.8f; // TODO: actually optimize
        PROGRESS_MOD = -.2f; // no idea what to make this TODO: don't just guess
        PROXIMITY_MOD = -2; // no idea... TODO: optimize
        IMPATIENCE_MOD = -.07f; // TODO: optimize
	}
	
	public void takeTurn() throws Exception{
        if(nearbyEnemyRobots.length > 0) {
        	//Let other robots know where you are!
        	if((rc.getRoundNum() +rc.getID()) % 25 == 0 || target == null){
				notifyFriendsOfEnemies(nearbyEnemyRobots);
			}
			int start = Clock.getBytecodeNum();
            doLumberjackMicro();
            System.out.println("micro took " + (Clock.getBytecodeNum() - start) + " bytecodes");
            if ( rc.canStrike() ){
                cutDownTrees();
            }
        } else {// don't need to worry about bullets, should always move safely
            // move to best tree-cutting location
            if (nearbyNeutralTrees.length + nearbyEnemyTrees.length > 0) {
                optimizeLocForWoodcutting(nearbyNeutralTrees, nearbyEnemyTrees);
                // chop best trees
                cutDownTrees();
            } else { // no trees in sight

                // TODO: move to where the scout tells us instead
                if(target == null){
                    assignNewTarget();
                }
        		else if (target != null && rc.getLocation().distanceTo(target) < 2){
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
        if (rc.canStrike() && !rc.hasMoved()){
            turnsWithoutMovingOrAttacking += 1;
        } else {
            System.out.println("I did something this turn?" + rc.canStrike() + rc.hasMoved());
            turnsWithoutMovingOrAttacking = 0;
        }
        System.out.println("turnsWithoutMovingOrAttacking: " + turnsWithoutMovingOrAttacking);
	}

	public void cutDownTrees() throws Exception{
        TreeInfo lowestStrengthNeutral = Util.leastHealthTouchingRadius(nearbyNeutralTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS);
        if (lowestStrengthNeutral != null && lowestStrengthNeutral.getHealth() <= GameConstants.LUMBERJACK_CHOP_DAMAGE) {
            // definitely prioritize, might have goodies
            rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0);
            rc.chop(lowestStrengthNeutral.getID());
            return;
        }
        // otherwise, consider other options...
        TreeInfo lowestStrengthEnemy = Util.leastHealthTouchingRadius(nearbyEnemyTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS);
        //just in case...
        if (lowestStrengthNeutral == null && lowestStrengthEnemy == null){
            System.out.println("phew, just saved us from an error");
            return;
        }

        if (lowestStrengthEnemy != null && lowestStrengthEnemy.getHealth() <= GameConstants.LUMBERJACK_CHOP_DAMAGE && lowestStrengthEnemy.getHealth() > RobotType.LUMBERJACK.attackPower) {
            // seems optimal to take out enemy trees when possible, but if low enough to strike then maybe do that
            rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0);
            rc.chop(lowestStrengthEnemy.getID());
        } else if (Util.containsBodiesTouchingRadius(nearbyAlliedRobots, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS)){
            // not safe to strike, would hit friends
            if (lowestStrengthEnemy != null) {
                rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0);
                rc.chop(lowestStrengthEnemy.getID());
            } else if(lowestStrengthNeutral!=null){
                rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0);
                rc.chop(lowestStrengthNeutral.getID());
            }
        } else {
            // could strike, is it a good idea?
            // if striking does more total damage
            if ((Util.numBodiesTouchingRadius(nearbyNeutralTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS) +
                    Util.numBodiesTouchingRadius(nearbyEnemyTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS))
                    * RobotType.LUMBERJACK.attackPower > GameConstants.LUMBERJACK_CHOP_DAMAGE) {
                rc.strike();
                rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
            } else { // chopping does more total damage
                if (lowestStrengthEnemy != null) {
                    rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0);
                    rc.chop(lowestStrengthEnemy.getID());
                } else if(lowestStrengthNeutral != null){
                    rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0);
                    rc.chop(lowestStrengthNeutral.getID());
                }
            }
        }
    }

    public void optimizeLocForWoodcutting(TreeInfo[] nearbyNeutralTrees, TreeInfo[] nearbyEnemyTrees) throws Exception{
	    // ONLY CALL WHEN there are actually tress nearby

        // TODO: actually optimize
        // for now just move towards the closest tree
        TreeInfo[] targets;
        if (nearbyEnemyTrees.length > 0) {
            targets = nearbyEnemyTrees;
        } else {
            targets = nearbyNeutralTrees;
        }
        goTo(targets[0].getLocation());
    }

	public void doLumberjackMicro() throws Exception{
	    // gets called when there are enemies that can be seen
        // don't worry about chopping trees here, that's checked for after. only enemies
        System.out.println("whee micro");

        // TODO: add kamikaze function: if about to die anyways, just go for best place to attack for final stand

        float attackScoreHere = evalForAttacking(here);
        float bestMoveScore = evaluateLocation(here) + (attackScoreHere < 0 ? 0 : MOVE_ATTACK_MOD * attackScoreHere) + IMPATIENCE_MOD * turnsWithoutMovingOrAttacking;
                        // there should be a way to remove the attack bit here such that
                        //     a better location won't be disregarded since we can attack
                        //     here and then move there
        System.out.println("here score " + bestMoveScore);
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
                rc.setIndicatorDot(currLoc, 0, 0, (int)(1.0*currentTheta / 360 * 255));
                attackScore = evalForAttacking(currLoc);
                score = evaluateLocation(currLoc) + (attackScoreHere < 0 ? 0 : MOVE_ATTACK_MOD * attackScoreHere);
                //                                  if you're not going to attack anyways, it doesn't matter how bad it is
                System.out.println(currentTheta + " " + currLoc.x + " " + currLoc.y + " score " + score);
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
                    System.out.print("I've tried everything dammit");
                    break;
                }
            }
        }
        System.out.println("tried " + numLocsEvaled + " locs and finished at theta " + currentTheta + " and radius " + stridedist);

        if ( attackScoreHere > bestLocAttackScore && attackScoreHere > 0){
            // attack first, then move
            rc.setIndicatorDot(here, 255,0,0); //red dot == SMASH
            rc.strike();
            rc.move(bestLoc);

        } else if (bestLocAttackScore > 0){
            // move first, then attack
            rc.move(bestLoc);
            rc.setIndicatorDot(bestLoc, 255,0,0); //red dot == SMASH
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
        }*/

        return KNOWN_DAMAGE_MOD * knownDamageToLoc(loc)
                + HYPOTHETICAL_DAMAGE_MOD * hypotheticalDamageToSpot(loc)
                + PROXIMITY_MOD * distToNearestEnemy
                + (target != null ? PROGRESS_MOD * here.distanceTo(target) - loc.distanceTo(target) : 0);
    }

    public float evalForAttacking(MapLocation loc){
        // how good it is to attack from this spot
        // score <= 0 means it's better not to attack
        float damageToThem = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyEnemyRobots, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        float damageToUs = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyAlliedRobots, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        float damageToEnemyTrees = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyEnemyTrees, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        float damageToAlliedTrees = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyAlliedTrees, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        return damageToThem - damageToUs + TREE_DAMAGE_MOD * (damageToEnemyTrees - damageToAlliedTrees);
    }
}