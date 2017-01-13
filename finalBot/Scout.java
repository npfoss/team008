package team008.finalBot;

import battlecode.common.*;

public class Scout extends Bot {

	public Scout(RobotController r){
		super(r);
	}

	public void takeTurn() throws Exception{

		nearbyAlliedRobots = rc.senseNearbyRobots(-1, us);
	    TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
        tryToHarass(nearbyTrees);
        dealWithNearbyTrees();
        explore();

	  // rc.setIndicatorDot(here,0,255,0);
	   RobotInfo[] enemies = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
		if(enemies.length > 0 && rc.getRoundNum() % 10 == 0){
			//rc.setIndicatorDot(enemies[0].location, 255, 0, 0);
			Util.notifyFriendsOfEnemies(enemies);
		}
       return;
	}



    public static boolean dealWithNearbyTrees() throws GameActionException{
		TreeInfo[] bulletTrees = new TreeInfo[nearbyNeutralTrees.length];
		int i = 0;
		for(TreeInfo tree: nearbyNeutralTrees){
			if(tree.containedBullets > 0){
				bulletTrees[i] = tree;
				i++;
			}
		}
		if(i == 0){
			return false;
		}
		TreeInfo closestBulletTree = Util.closestTree(bulletTrees, rc.getLocation(), i);
		goTo(closestBulletTree.location);

		return true;
	}

    /**
     * Checks how good of a spot we're in and tries to take out archon/gardeners we have a clear shot at
     * @param nearbyTrees Array of all trees we can see
     * @throws GameActionException
     */
    private void tryToHarass(TreeInfo[] nearbyTrees) throws GameActionException {
        RobotInfo closesEnemyArchon = Util.closestSpecificTypeOnTeam(nearbyRobots,here, RobotType.ARCHON,enemy);
        RobotInfo closesEnemyGardener = Util.closestSpecificTypeOnTeam(nearbyRobots,here,RobotType.GARDENER,enemy);
        RobotInfo target = ( closesEnemyGardener!=null )? closesEnemyGardener:closesEnemyArchon;
        if(target!=null) {
            TreeInfo bestTree = Util.closestTree(nearbyTrees, target.location);
            if (inGoodSpot(bestTree)) {
                rc.setIndicatorDot(here,0,0,255);
                harassFromTree(target);
            } else{
                goTo(bestTree.location);
            }
        }
    }

    /**
     * Checks how close we are to our target tree
     * @param closestTreeToEnemy the tree we want to be close to
     * @return
     */
    private static boolean inGoodSpot(TreeInfo closestTreeToEnemy){
        return closestTreeToEnemy.location.distanceTo(here) < 5;
    }

    /**
     * Attempts to get in the closest tree and shoot at archons/gardeners
     * @param closestTarget BodyInfo object
     * @return true if we are in the spot we want to be in
     * @throws GameActionException
     */
	public static void harassFromTree(RobotInfo closestTarget) throws GameActionException {
        if (closestTarget != null) {
            rc.setIndicatorLine(here,closestTarget.getLocation(),255,0,0);
                 RangedCombat.shootSingleShot(closestTarget);
        }
    }

    /**
     * NOT USED
     * Attempts to check if we have a clear shot to the target
     * @param closestTarget teh target we want to shoot
     * @return true if we have a clear shot
     */
//    private static boolean haveAClearShot(RobotInfo closestTarget) throws GameActionException {
//        Direction intendedAttackDir = here.directionTo(closestTarget.location);
//        for(RobotInfo robot: nearbyRobots){
//            if(intendedAttackDir.radiansBetween(here.directionTo(robot.location)) < Math.PI/10 && robot !=closestTarget){
//                return false;
//            }
//        }
//
//        return true;
//    }

}