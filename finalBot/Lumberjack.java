package team008.finalBot;

import battlecode.common.*;

public class Lumberjack extends Bot {
	public Lumberjack(RobotController r){
		super(r);
		//anything else lumberjack specific
	}
	
	public void takeTurn(TreeInfo[] nearbyNeutralTrees) throws Exception{
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(99, enemy);
        RobotInfo[] nearbyFriends = rc.senseNearbyRobots(99, us);
        // TreeInfo[] nearbyEnemyTrees = rc.senseNearbyTrees(99, enemy);
        // ^ this was put elsewhere because it might not need to happen if we doMicro

        if(nearbyEnemies.length > 0) {
        	//Let other robots know where you are!
        	if(rc.getRoundNum() % 25 == 0){
				Util.notifyFriendsOfEnemies(nearbyEnemies);
			}
            // Use strike() to hit all nearby robots!
            doLumberjackMicro(nearbyFriends, nearbyEnemies);
            if ( rc.canStrike() ){
                TreeInfo[] nearbyEnemyTrees = rc.senseNearbyTrees(99, enemy);
                cutDownTrees(nearbyNeutralTrees, nearbyEnemyTrees, nearbyFriends);
            }
        } else {// don't need to worry about bullets, should always move safely
            TreeInfo[] nearbyEnemyTrees = rc.senseNearbyTrees(99, enemy);

            // move to best tree-cutting location
            if (nearbyNeutralTrees.length + nearbyEnemyTrees.length > 0) {
                optimizeLocForWoodcutting(nearbyNeutralTrees, nearbyEnemyTrees);
            } else { // no trees in sight
                // for now just move towards enemy
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
        			tryMoveDirection(here.directionTo(Util.rc.getInitialArchonLocations(enemy)[0]));
        		}
            }
            // chop best trees
            cutDownTrees(nearbyNeutralTrees, nearbyEnemyTrees, nearbyFriends);

//            // No close robots, so search for robots within sight radius
//            robots = rc.senseNearbyRobots(-1,enemy);
//
//            // If there is a robot, move towards it
//            if(robots.length > 0) {
//                MapLocation myLocation = rc.getLocation();
//                MapLocation enemyLocation = robots[0].getLocation();
//                Direction toEnemy = myLocation.directionTo(enemyLocation);
//
//                tryMove(toEnemy);
//            } else {
//                // Move Randomly
//                tryMove(Util.randomDirection());
//            }
        }
	}

	public void cutDownTrees(TreeInfo[] nearbyNeutralTrees, TreeInfo[] nearbyEnemyTrees, RobotInfo[] nearbyAllies) throws Exception{
        TreeInfo lowestStrengthNeutral = Util.leastHealthTouchingRadius(nearbyNeutralTrees, rc.getLocation(), RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS);
        boolean prioritizeNeutral = false;
        if (lowestStrengthNeutral != null && lowestStrengthNeutral.getHealth() <= GameConstants.LUMBERJACK_CHOP_DAMAGE) {
            // definitely prioritize, might have goodies
            if (lowestStrengthNeutral.getHealth() > RobotType.LUMBERJACK.attackPower) {
                rc.chop(lowestStrengthNeutral.getID());
                rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0);
                return;
            } else {
                prioritizeNeutral = true;
            }
        }
        // otherwise, consider other options...
        TreeInfo lowestStrengthEnemy = Util.leastHealthTouchingRadius(nearbyEnemyTrees, rc.getLocation(), RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS);
        if (!prioritizeNeutral && lowestStrengthEnemy != null && lowestStrengthEnemy.getHealth() <= GameConstants.LUMBERJACK_CHOP_DAMAGE && lowestStrengthEnemy.getHealth() > RobotType.LUMBERJACK.attackPower) {
            // seems optimal to take out enemy trees when possible, but if low enough to strike then maybe do that
            rc.chop(lowestStrengthEnemy.getID());
            rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0);
        } else if (Util.containsBodiesTouchingRadius(nearbyAllies, rc.getLocation(), RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS)){
            // not safe to strike, would hit friends
            if (prioritizeNeutral) {
                rc.chop(lowestStrengthNeutral.getID());
                rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0);
            } else if (lowestStrengthEnemy != null) {
                rc.chop(lowestStrengthEnemy.getID());
                rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0);
            } else {
                rc.chop(lowestStrengthNeutral.getID());
                rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0);
            }
        } else {
            // could strike, is it a good idea?
            // if striking does more total damage
            if ((Util.numBodiesTouchingRadius(nearbyNeutralTrees, rc.getLocation(), RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS) +
                    Util.numBodiesTouchingRadius(nearbyEnemyTrees, rc.getLocation(), RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS))
                    * RobotType.LUMBERJACK.attackPower > GameConstants.LUMBERJACK_CHOP_DAMAGE) {
                rc.strike();
            } else { // chopping does more total damage
                if (prioritizeNeutral) {
                    rc.chop(lowestStrengthNeutral.getID());
                    rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0);
                } else if (lowestStrengthEnemy != null) {
                    rc.chop(lowestStrengthEnemy.getID());
                    rc.setIndicatorLine(rc.getLocation(), lowestStrengthEnemy.getLocation(),0, 255, 0);
                } else {
                    rc.chop(lowestStrengthNeutral.getID());
                    rc.setIndicatorLine(rc.getLocation(), lowestStrengthNeutral.getLocation(),0, 255, 0);
                }
            }
        }
        if(rc.canStrike()){
            System.out.println("uh oh, didn't chop");
        }
    }

    public void optimizeLocForWoodcutting(TreeInfo[] nearbyNeutralTrees, TreeInfo[] nearbyEnemyTrees) throws Exception{
	    // ONLY CALLED WHEN there are actually tress nearby

        // for now just move towards the closest tree
        TreeInfo[] targets;
        if (nearbyEnemyTrees.length > 0) {
            targets = nearbyEnemyTrees;
        } else {
            targets = nearbyNeutralTrees;
        }
        goTo(Util.closestBody(targets, rc.getLocation()).getLocation());

        // TODO: actually optimize
    }

	public void doLumberjackMicro(RobotInfo[] nearbyFriends, RobotInfo[] nearbyEnemies) throws Exception{
	    // gets called when there are enemies that can be seen
        // don't worry about chopping trees here, that's checked for after. only enemies
        rc.setIndicatorDot(rc.getLocation(), 255,0,0); //red dot == doing micro

        //BulletInfo[] nearbyBullets = rc.senseNearbyBullets();

        // TODO: make this a lot more intelligent

        // strike only if it does more damage to the enemy team than us
        if (Util.numBodiesTouchingRadius(nearbyEnemies, rc.getLocation(), RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS)
                > Util.numBodiesTouchingRadius(nearbyFriends, rc.getLocation(), RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS)){
            rc.strike();
        }

        // charge closest enemy, ~safely~ (lol)
        goTo(Util.closestBody(nearbyEnemies, rc.getLocation()).getLocation());

        // check again if we can strike
        if (rc.canStrike() && Util.numBodiesTouchingRadius(nearbyEnemies, rc.getLocation(), RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS)
                > Util.numBodiesTouchingRadius(nearbyFriends, rc.getLocation(), RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS)){
            rc.strike();
        }
    }
}