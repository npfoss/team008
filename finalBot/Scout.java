package team008.finalBot;

import battlecode.common.*;

public class Scout extends Bot {
	static boolean triedInitLocation;
	static MapLocation closestInitLocation;

	public Scout(RobotController r) throws GameActionException{
		super(r);
	}
	
	public void takeTurn() throws Exception{
		if(closestInitLocation == null){
			MapLocation[] initLocations = rc.getInitialArchonLocations(enemy);
			closestInitLocation = Util.closestLocation(initLocations, here);
		}

	    //TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
        if(!tryToHarass(Util.combineTwoTIArrays(nearbyEnemyTrees, nearbyNeutralTrees))){
        	if(!dealWithNearbyTrees()){
        		moveToHarass();
        	}
        }

	  // rc.setIndicatorDot(here,0,255,0);
		if(nearbyEnemyRobots.length > 0 && rc.getRoundNum() % 10 == 0){
			//rc.setIndicatorDot(enemies[0].location, 255, 0, 0);
			Util.notifyFriendsOfEnemies(nearbyEnemyRobots);
		}
       return;
	}
	
	public static void moveToHarass() throws GameActionException{
		if(!triedInitLocation){
			if(here.distanceTo(closestInitLocation) < 2){
				triedInitLocation = true;
				explore();
			}
			else{
				goTo(closestInitLocation);
			}
		}
		else{
			explore();
		}
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
    private boolean tryToHarass(TreeInfo[] nearbyTrees) throws GameActionException {
        //RobotInfo closestEnemyArchon = Util.closestSpecificTypeOnTeam(nearbyRobots,here, RobotType.ARCHON,enemy);
        RobotInfo closestEnemyGardener = Util.closestSpecificTypeOnTeam(nearbyRobots,here,RobotType.GARDENER,enemy);
        RobotInfo target = closestEnemyGardener;
        if(target!=null) {
            TreeInfo bestTree = Util.closestTree(nearbyTrees, target.location);
            MapLocation outerEdge = bestTree.location.add(bestTree.location.directionTo(target.location),bestTree.radius);
            MapLocation targetLoc = outerEdge.add(outerEdge.directionTo(bestTree.location), (float)(1.01));
            if(bestTree.location.distanceTo(target.location) > 7){
            	return false;
            }
            if (inGoodSpot(targetLoc, target)) {
                //rc.setIndicatorDot(here,0,0,255);
                harassFromTree(target);
            } else{
            	rc.setIndicatorLine(here,targetLoc,0,0,255);
            	//System.out.println(here.distanceTo(bestTree.location));
                goTo(targetLoc);
            }
            return true;
        }
        return false;
    }

    /**
     * Checks how close we are to our target tree
     * @param closestTreeToEnemy the tree we want to be close to
     * @return
     * @throws GameActionException 
     */
    private static boolean inGoodSpot(MapLocation targetLoc, RobotInfo target) throws GameActionException{
        return targetLoc.distanceTo(here) < 0.01;
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
            System.out.println("I shot");
        }
    }

    /**
     * NOT USED
     * Attempts to check if we have a clear shot to the target
     * @param closestTarget the target we want to shoot
     * @return true if we have a clear shot
     */
    private static boolean haveAClearShot(RobotInfo closestTarget) throws GameActionException {
        Direction intendedAttackDir = here.directionTo(closestTarget.location);
        for(RobotInfo robot: nearbyRobots){
            if(intendedAttackDir.radiansBetween(here.directionTo(robot.location)) < Math.PI/10 && robot !=closestTarget){
                return false;
            }
        }
        
        TreeInfo[] nearbyTrees = Util.combineTwoTIArrays(nearbyEnemyTrees, nearbyNeutralTrees);
        
        for(TreeInfo tree: nearbyTrees){
            if(intendedAttackDir.radiansBetween(here.directionTo(tree.location)) < Math.PI/10){
                return false;
            }
        }

        return true;
    }

}