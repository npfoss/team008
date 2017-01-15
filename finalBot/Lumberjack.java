package team008.finalBot;

import battlecode.common.*;

public class Lumberjack extends Bot {
    public int WHEN_TO_STOP_MICRO;

	public Lumberjack(RobotController r) throws GameActionException{
		super(r);
		//anything else lumberjack specific
        WHEN_TO_STOP_MICRO = RobotType.LUMBERJACK.bytecodeLimit - 2000; //TODO: don't just guess
	}
	
	public void takeTurn() throws Exception{
        if(nearbyEnemyRobots.length > 0) {
        	//Let other robots know where you are!
        	if(rc.getRoundNum() % 25 == 0){
				notifyFriendsOfEnemies(nearbyEnemyRobots);
			}
            // Use strike() to hit all nearby robots!
            doLumberjackMicro(nearbyAlliedRobots, nearbyEnemyRobots);
            if ( rc.canStrike() ){
                cutDownTrees();
            }
        } else {// don't need to worry about bullets, should always move safely
            // move to best tree-cutting location
            if (nearbyNeutralTrees.length + nearbyEnemyTrees.length > 0) {
                optimizeLocForWoodcutting(nearbyNeutralTrees, nearbyEnemyTrees);
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
            // chop best trees
            cutDownTrees();
        }
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
                //rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0);
            } else if(lowestStrengthNeutral!=null){
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
                    //rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0);
                } else if(lowestStrengthNeutral != null){
                    rc.chop(lowestStrengthNeutral.getID());
                }
            }
        }
        if(rc.canStrike()){
            //System.out.println("uh oh, didn't chop");
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
        goTo(Util.closestBody(targets, rc.getLocation()).getLocation());
    }

	public void doLumberjackMicro(RobotInfo[] nearbyFriends, RobotInfo[] nearbyEnemies) throws Exception{
	    // gets called when there are enemies that can be seen
        // don't worry about chopping trees here, that's checked for after. only enemies

        //rc.setIndicatorDot(rc.getLocation(), 255,0,0); //red dot == doing micro
        // TODO: make this a lot more intelligent

        /*float bestMoveScore = evaluateLocation(rc.getLocation());
        float score;
        MapLocation bestLoc = rc.getLocation();
        float attackScoreHere = evalForAttacking(bestLoc);
        int numLocsEvaled = 0;
        MapLocation currLoc;
        int startTheta = 0;
        int currentTheta = 0;
        float stridedist = RobotType.LUMBERJACK.strideRadius;
        int startBytecode = Clock.getBytecodeNum();
        while (Clock.getBytecodeNum() + (1.0*Clock.getBytecodeNum() - startBytecode)/numLocsEvaled < WHEN_TO_STOP_MICRO){
            currLoc = rc.getLocation().add(Util.radians(currentTheta), stridedist);
            score = evaluateLocation(currLoc) +
        }*/



        /// ----- OLD MICRO -----
        // strike only if it does more damage to the enemy team than us
        if (Util.numBodiesTouchingRadius(nearbyEnemies, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS)
                > Util.numBodiesTouchingRadius(nearbyFriends, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS)){
            rc.strike();
        }

        // charge closest enemy, ~safely~ (lol)
        goTo(Util.closestBody(nearbyEnemies, rc.getLocation()).getLocation());

        // check again if we can strike
        if (rc.canStrike() && Util.numBodiesTouchingRadius(nearbyEnemies, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS)
                > Util.numBodiesTouchingRadius(nearbyFriends, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS)){
            rc.strike();
        }//*/
    }

    public int evaluateLocation(MapLocation loc){
	    // 'scores' the location in terms of possible damage accrued (bullets and otherwise)
        //     and strategery, but NOT attacking damage

        return 0;
    }
}