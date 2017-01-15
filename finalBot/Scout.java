package team008.finalBot;

import battlecode.common.*;

public class Scout extends Bot {
	private static boolean triedInitLocation;
	private static MapLocation closestInitLocation;
	private static MapLocation targetLoc;
	private static int targetGardenerID;

	public Scout(RobotController r) throws GameActionException{
		super(r);
		targetGardenerID = -1;
	}

	public void takeTurn() throws Exception{
		if(closestInitLocation == null){
			MapLocation[] initLocations = rc.getInitialArchonLocations(enemy);
			closestInitLocation = Util.closestLocation(initLocations, here);
		}

	    //TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
		/*
		int numHostiles = Util.numHostileUnits(nearbyEnemyRobots);
		if(numHostiles > 0){
			System.out.println("Ranged Combat");
			RangedCombat.execute();
		}
		*/
		if(!tryToHarass(Util.combineTwoTIArrays(nearbyEnemyTrees, nearbyNeutralTrees))){
        	if(!dealWithNearbyTrees()){
        		moveToHarass();
        	}
        }

	  // rc.setIndicatorDot(here,0,255,0);
		if(nearbyEnemyRobots.length > 0 && rc.getRoundNum() % 10 == 0){
			//rc.setIndicatorDot(enemies[0].location, 255, 0, 0);
			notifyFriendsOfEnemies(nearbyEnemyRobots);
		}
       return;
	}
	
	public static void moveToHarass() throws GameActionException{
		if(!triedInitLocation){
			if(here.distanceTo(closestInitLocation) < 3){
				triedInitLocation = true;
				explore();
			}
			else{
				goTo(closestInitLocation);
			}
		}
		else{
			//RobotInfo closestArchon = Util.closestSpecificTypeOnTeam(nearbyRobots,here, RobotType.ARCHON,enemy);
			//if(closestArchon != null){
			//	goTo(closestArchon.location);
			//	return;
			//}
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
    	RobotInfo targetG = null;
    	if(inDanger(nearbyEnemyRobots, nearbyBullets)){
    		return false;
    	}
    	if(targetGardenerID == -1 || !rc.canSenseRobot(targetGardenerID) || rc.getRoundNum() % 10 == 1){
    		targetGardenerID = -1;
    		targetLoc = null;
    		updateTargetGardener();
    		if(targetGardenerID != -1){
    			System.out.println("new target gardener: id = " + targetGardenerID);
    			targetG = rc.senseRobot(targetGardenerID);
    			updateTargetLoc(nearbyTrees, targetG);
    		}
    	}
        if(targetGardenerID != -1) {
        	targetG = rc.senseRobot(targetGardenerID);
        	if(targetLoc == null && rc.getRoundNum() % 5 == 3){
        		updateTargetLoc(nearbyTrees, targetG);
        	}
        	if(targetLoc == null){
            	System.out.println("can't find a tree, but still trying to kill gardener");
            	if(here.distanceTo(targetG.location) < 2.5){
            		RangedCombat.shootSingleShot(targetG);
            	}
            	else{
            		goTo(targetG.location);
            	}
            }
        	else if (inGoodSpot(targetLoc)) {
                //rc.setIndicatorLine(here,targetG.location,0,0,255);
                harassFromTree(targetG);
            } else{
            	//rc.setIndicatorLine(here,targetLoc,0,0,255);
            	System.out.println("heading toward tree");
            	//System.out.println(here.distanceTo(bestTree.location));
                goTo(targetLoc);
            }
            return true;
        }
        return false;
    }

    private boolean inDanger(RobotInfo[] nearbyEnemyRobots, BulletInfo[] nearbyBullets) {
		BulletInfo closestBullet = Util.closestBullet(nearbyBullets, here);
		if(closestBullet != null && here.distanceTo(closestBullet.location) < 3){
			System.out.println("in danger.");
			return true;
		}
		for(RobotInfo e: nearbyEnemyRobots){
			if(e.type == RobotType.ARCHON || e.type == RobotType.GARDENER)
				continue;
			else if(e.location.distanceTo(here) < 3){
				System.out.println("in danger.");
				return true;
			}
		}
		return false;
	}

	private static void updateTargetGardener(){
        RobotInfo closestEnemyGardener = closestUndefendedGardener(nearbyEnemyRobots);
        if(closestEnemyGardener != null){
        	targetGardenerID = closestEnemyGardener.ID;
        }
    }
    
    private static void updateTargetLoc(TreeInfo[] nearbyTrees, RobotInfo targetG){
        TreeInfo bestTree = closestSafeTree(nearbyTrees, targetG.location);
        if(bestTree != null){
        	MapLocation outerEdge = bestTree.location.add(bestTree.location.directionTo(targetG.location),bestTree.radius);
            targetLoc = outerEdge.add(outerEdge.directionTo(bestTree.location), (float)(1.01));
        }
    }
    
    public static TreeInfo closestSafeTree(TreeInfo[] trees, MapLocation toHere) {
        TreeInfo closest = null;
        float bestDist = 5;
        float dist;
        for (int i = trees.length; i-- > 0;) {
            dist = toHere.distanceTo(trees[i].location);
            if(dist < bestDist){
	            RobotInfo closestAlly = Util.closestRobot(nearbyAlliedRobots, trees[i].location);
	            if(closestAlly == null || closestAlly.location.distanceTo(trees[i].location) > 2.5) {
	            	RobotInfo[] enemiesWithinRangeOfTree = rc.senseNearbyRobots(trees[i].location, 3, enemy);
	            	if(consistsOfOnlyHarmlessUnits(enemiesWithinRangeOfTree)){
		                bestDist = dist;
		                closest = trees[i];
	            	}
	            }
            }
        }
        return closest;
    }
    
    private static RobotInfo closestUndefendedGardener(RobotInfo[] robots){
    	RobotInfo closest = null;
        float bestDist = 99999;
        float dist;
        for (int i = robots.length; i-- > 0;) {
            if (robots[i].type == RobotType.GARDENER) {
                dist = here.distanceTo(robots[i].location);
               //RobotInfo[] enemiesWithinRangeOfGardener = rc.senseNearbyRobots(robots[i].location, 3, enemy);
                if (dist < bestDist/* && consistsOfOnlyHarmlessUnits(enemiesWithinRangeOfGardener)*/) {
                    bestDist = dist;
                    closest = robots[i];
                }
            }
        }
        return closest;
    }
    
    private static boolean consistsOfOnlyHarmlessUnits(RobotInfo[] enemiesWithinRangeOfGardener) {
		for(RobotInfo e: enemiesWithinRangeOfGardener){
			if(e.type != RobotType.ARCHON && e.type != RobotType.GARDENER){
				return false;
			}
		}
		return true;
	}

	/**
     * Checks how close we are to our target tree
     * @param targetLoc the tree we want to be close to
     * @return
     * @throws GameActionException 
     */
    private static boolean inGoodSpot(MapLocation targetLoc) throws GameActionException{
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
            //rc.setIndicatorLine(here,closestTarget.getLocation(),255,0,0);
            RangedCombat.shootSingleShot(closestTarget);
            //System.out.println("I shot");
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