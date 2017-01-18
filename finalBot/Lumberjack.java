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
    public float GARDENER_PROXIMITY_MOD;
    public int turnsWithoutMovingOrAttacking;
    private static boolean isDefender;

	public Lumberjack(RobotController r) throws GameActionException{
		super(r);
		debug = true;
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
		//anything else lumberjack specific
        WHEN_TO_STOP_MICRO = RobotType.LUMBERJACK.bytecodeLimit - 2000; //TODO: don't just guess
        MOVE_ATTACK_MOD = 1; // TODO: actually optimize
        TREE_DAMAGE_MOD = .2f; // TODO: actually optimize
        KNOWN_DAMAGE_MOD = -1.2f;
        HYPOTHETICAL_DAMAGE_MOD = -1f; // TODO: actually optimize
        PROGRESS_MOD = -.2f; // no idea what to make this TODO: don't just guess
        PROXIMITY_MOD = -3; // no idea... TODO: optimize
        IMPATIENCE_MOD = -.12f; // TODO: optimize
        GARDENER_PROXIMITY_MOD = -7;
	}
	
	public void takeTurn() throws Exception{
		/*if(isDefender){
			System.out.println("I am a defender");
			if(rc.canSenseLocation(gardenerLoc) && rc.senseRobotAtLocation(gardenerLoc) == null){
				isDefender = false;
			}
			else if(nearbyEnemyRobots.length > 0){
				if(rc.canSenseLocation(gardenerLoc)){
					//System.out.println("defending");
					DefenseMicro.defendL(rc.senseRobotAtLocation(gardenerLoc));
				}
				else{
					doLumberjackMicro();
				}
			}
			/*
			else if (target == null){
				MapLocation dis = Messaging.getClosestDistressSignal(here);
				if(dis!= null && here.distanceTo(dis) < 15){
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
        if(isDefender) {
            System.out.println("I am a defender");
            if (rc.canSenseLocation(gardenerLoc) && rc.senseRobotAtLocation(gardenerLoc) == null) {
                isDefender = false;
            }
        } else {
            RobotInfo gardener = Util.firstUnitOfType(nearbyAlliedRobots, RobotType.GARDENER);
            if( gardener != null ){
                isDefender = true;
                gardenerLoc = gardener.getLocation();
            }
		}

        if(nearbyEnemyRobots.length > 0) {
            //Let other robots know where you are!
            if((rc.getRoundNum() +rc.getID()) % 25 == 0 || target == null){
                notifyFriendsOfEnemies(nearbyEnemyRobots);
            }
            int start = Clock.getBytecodeNum();
            doLumberjackMicro();
            //System.out.println("micro took " + (Clock.getBytecodeNum() - start) + " bytecodes");
        } else {
            if(target == null){
                assignNewTarget();
            }
            if (!isDefender && target != null && Util.distanceSquaredTo(here, target) < 15 && nearbyEnemyRobots.length + nearbyEnemyTrees.length == 0){
                if (debug) { System.out.print("removing distress loc... "); }
                if( !Messaging.removeDistressLocation(target)) {
                    if (debug) { System.out.println("failed"); }
                    Messaging.removeEnemyTreeLocation(target);
                }
                target = null;
                assignNewTarget();
            }
            // TODO: try this out
            /*if( isDefender ) {
                circleLocAtRadius(gardenerLoc, RobotType.GARDENER.bodyRadius + 2 * GameConstants.BULLET_TREE_RADIUS + RobotType.LUMBERJACK.bodyRadius);
            } else*/ if(target != null){
                goTo(target);
            } else{
                if (nearbyNeutralTrees.length + nearbyEnemyTrees.length > 0) {
                    // move to best tree-cutting location
                    optimizeLocForWoodcutting(nearbyNeutralTrees, nearbyEnemyTrees);
                    // chop best trees
                    cutDownTrees();
                } else {
                    goTo(here.directionTo(Util.rc.getInitialArchonLocations(enemy)[0]));
                }
            }
        }
        if ( rc.canStrike() ){
            cutDownTrees();
        }

        if (rc.canStrike() && !rc.hasMoved()){
            turnsWithoutMovingOrAttacking += 1;
        } else {
            if (debug) { System.out.println("I did something this turn?" + rc.canStrike() + rc.hasMoved()); }
            turnsWithoutMovingOrAttacking = 0;
        }
        if (debug) { System.out.println("turnsWithoutMovingOrAttacking: " + turnsWithoutMovingOrAttacking); }
	}

    public void assignNewTarget() throws GameActionException {
        target = Messaging.getClosestEnemyTreeLocation(here);
        MapLocation alt = Messaging.getClosestDistressSignal(here);
        if (target == null || (alt != null && Util.distanceSquaredTo(here, alt) < Util.distanceSquaredTo(here, target))) {
            target = alt;
        }
        if (debug && target != null) { System.out.println("new target: " + target.x + " " + target.y); }
    }

	public void cutDownTrees() throws Exception{
        TreeInfo lowestStrengthNeutral = Util.leastHealthTouchingRadius(nearbyNeutralTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS);
        if (lowestStrengthNeutral != null && lowestStrengthNeutral.getHealth() <= GameConstants.LUMBERJACK_CHOP_DAMAGE) {
            // definitely prioritize, might have goodies
            if (debug) { rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0); }
            rc.chop(lowestStrengthNeutral.getID());
            return;
        }
        // otherwise, consider other options...
        TreeInfo lowestStrengthEnemy = Util.leastHealthTouchingRadius(nearbyEnemyTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS);
        //just in case...
        if (lowestStrengthNeutral == null && lowestStrengthEnemy == null){
            //System.out.println("phew, just saved us from an error");
            return;
        }

        if (lowestStrengthEnemy != null && lowestStrengthEnemy.getHealth() <= GameConstants.LUMBERJACK_CHOP_DAMAGE && lowestStrengthEnemy.getHealth() > RobotType.LUMBERJACK.attackPower) {
            // seems optimal to take out enemy trees when possible, but if low enough to strike then maybe do that
            if (debug) { rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0); }
            rc.chop(lowestStrengthEnemy.getID());
        } else if (Util.containsBodiesTouchingRadius(nearbyAlliedRobots, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS)){
            // not safe to strike, would hit friends
            if (lowestStrengthEnemy != null) {
                if (debug) { rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0); }
                rc.chop(lowestStrengthEnemy.getID());
            } else if(lowestStrengthNeutral!=null){
                if (debug) { rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0); }
                rc.chop(lowestStrengthNeutral.getID());
            }
        } else {
            // could strike, is it a good idea?
            // if striking does more total damage
            if ((Util.numBodiesTouchingRadius(nearbyNeutralTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS) +
                    Util.numBodiesTouchingRadius(nearbyEnemyTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS))
                    * RobotType.LUMBERJACK.attackPower > GameConstants.LUMBERJACK_CHOP_DAMAGE) {
                rc.strike();
                if (debug) { rc.setIndicatorDot(rc.getLocation(), 0, 255, 0); }
            } else { // chopping does more total damage
                if (lowestStrengthEnemy != null) {
                    if (debug) { rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0); }
                    rc.chop(lowestStrengthEnemy.getID());
                } else if(lowestStrengthNeutral != null){
                    if (debug) { rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0); }
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
        if(!isDefender || targets[0].location.distanceTo(gardenerLoc) < 5)
        	goTo(targets[0].location);
    }

	public void doLumberjackMicro() throws Exception{
	    // gets called when there are enemies that can be seen
        // don't worry about chopping trees here, that's checked for after. only enemies
        if (debug) { System.out.println("whee micro"); }

        // TODO: add kamikaze function: if about to die anyways, just go for best place to attack for final stand

        // if you're defending and they're on the other side of the circle, don't bother calculating attack stuff
        if (isDefender && nearbyEnemyRobots[0].getType() == RobotType.SCOUT && here.distanceTo(nearbyEnemyRobots[0].getLocation()) > 4){
            goTo(nearbyEnemyRobots[0].getLocation());
            return;
        }

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
        }*/

        return KNOWN_DAMAGE_MOD * knownDamageToLoc(loc)
                + HYPOTHETICAL_DAMAGE_MOD * hypotheticalDamageToSpot(loc)
                + PROXIMITY_MOD * distToNearestEnemy
                + (target != null ? PROGRESS_MOD * here.distanceTo(target) - loc.distanceTo(target) : 0)
                + (gardenerLoc != null ? GARDENER_PROXIMITY_MOD * (loc.distanceTo(gardenerLoc) < 3.6 ? 3.6f - loc.distanceTo(gardenerLoc) : 0) : 0 )
                    // translation: if too close to gardener I'm defending, it's bad. (3.6 isn't random, it's sqrt(3)*3/2)
                ;
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