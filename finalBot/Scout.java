package team008.finalBot;

import battlecode.common.*;

public class Scout extends Bot {
	private static MapLocation[] initLocations;
	private static int locNum;
	private static MapLocation targetLoc;
	private static int targetGardenerID;
	private static boolean searchingForEdges;
	private static boolean foundMaxX;
	private static boolean foundMaxY;
	private static boolean foundMinX;
	private static boolean foundMinY;

	public Scout(RobotController r) throws GameActionException {
		super(r);
		searchingForEdges = true;
		targetGardenerID = -1;
		initLocations = MapAnalysis.initialEnemyArchonLocations;
		locNum = 0;
		foundMaxX = false;
		foundMaxY = false;
		foundMinY = false;
		foundMaxX = false;
	}

	public void takeTurn() throws Exception {
		// TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
		/*
		 * int numHostiles = Util.numHostileUnits(nearbyEnemyRobots);
		 * if(numHostiles > 0){ System.out.println("Ranged Combat");
		 * RangedCombat.execute(); }
		 */

		if (!tryToHarass(nearbyTrees)) {
			if (!dealWithNearbyTrees()) {
				moveToHarass();
			}
		}

		// rc.setIndicatorDot(here,0,255,0);
		if (nearbyEnemyRobots.length > 0 && rc.getRoundNum() +rc.getID() % 25 == 0) {
			// rc.setIndicatorDot(enemies[0].location, 255, 0, 0);
			notifyFriendsOfEnemies(nearbyEnemyRobots);
		}
		
		if(Clock.getBytecodesLeft() > 3000 && searchingForEdges){
			searchForEdges();
		}
		return;
	}

	private void searchForEdges() throws GameActionException {
		if(foundMaxX && foundMinY && foundMinX && foundMaxY){
			searchingForEdges = false;
			return;
		}
		//System.out.println("searching");
		boolean updated = false;
	    float minX = Messaging.getMinX();
	    float minY = Messaging.getMinY();
		float maxX = Messaging.getMaxX();
		float maxY = Messaging.getMaxY();
		//System.out.println(Math.abs(maxY));
		if(minX == 0 && !foundMinX){
			MapLocation edge = checkForEdge(Direction.getWest());
			if(edge != null){
				Messaging.updateMinX(edge.x);
				minX = edge.x;
				updated = true;
				foundMinX = true;
				System.out.println("updated min x to " + minX);
			}
		}
		if(minY == 0 && !foundMinY){
			MapLocation edge = checkForEdge(Direction.getSouth());
			if(edge != null){
				Messaging.updateMinY(edge.y);
				minY = edge.y;
				updated = true;
				foundMinY = true;
				System.out.println("updated min y to " + minY);
			}
		}
		if(maxX == 0 && !foundMaxX){
			MapLocation edge = checkForEdge(Direction.getEast());
			if(edge != null){
				Messaging.updateMaxX(edge.x);
				maxX = edge.x;
				updated = true;
				foundMaxX = true;
				System.out.println("updated max x to " + maxX);
			}
		}
		if(maxY == 0 && !foundMaxY){
			MapLocation edge = checkForEdge(Direction.getNorth());
			if(edge != null){
				Messaging.updateMaxY(edge.y);
				maxY = edge.y;
				updated = true;
				foundMaxY = true;
				System.out.println("updated max y to " + maxY);
			}
		}
		if(updated){
			float area = (maxX - minX) * (maxY - minY);
			Messaging.updateArea(area);
			System.out.println("area = " + area);
		}
	}

	private MapLocation checkForEdge(Direction dir) throws GameActionException {
		float highDist = type.sensorRadius;
		if(rc.onTheMap(here.add(dir,highDist - (float)(0.01))))
			return null;
		float lowDist = type.bodyRadius;
		while(highDist - lowDist > .01){
			float midDist = (highDist + lowDist) / 2;
			if(rc.onTheMap(here.add(dir,midDist))){
				lowDist = midDist;
			}
			else{
				highDist = midDist;
			}	
		}
		return here.add(dir,highDist);
	}

	public static void moveToHarass() throws GameActionException {
		if (locNum < initLocations.length) {
			if (here.distanceTo(initLocations[locNum]) < 5) {
				locNum++;
				explore();
			} else {
				goTo(initLocations[locNum]);
			}
		} else {
			// RobotInfo closestArchon =
			// Util.closestSpecificTypeOnTeam(nearbyRobots,here,
			// RobotType.ARCHON,enemy);
			// if(closestArchon != null){
			// goTo(closestArchon.location);
			// return;
			// }
			explore();
		}
	}

	public static boolean dealWithNearbyTrees() throws GameActionException {
		TreeInfo[] bulletTrees = new TreeInfo[nearbyNeutralTrees.length];
		int i = 0;
		for (TreeInfo tree : nearbyNeutralTrees) {
			if (tree.containedBullets > 0) {
				bulletTrees[i] = tree;
				i++;
			}
		}
		if (i == 0) {
			return false;
		}
		TreeInfo closestBulletTree = Util.closestTree(bulletTrees, rc.getLocation(), i);

		goTo(closestBulletTree.location);

		return true;
	}

	/**
	 * Checks how good of a spot we're in and tries to take out archon/gardeners
	 * we have a clear shot at
	 * 
	 * @param nearbyTrees
	 *            Array of all trees we can see
	 * @throws GameActionException
	 */
	private boolean tryToHarass(TreeInfo[] nearbyTrees) throws GameActionException {
		RobotInfo targetG = null;
		if (inDanger(nearbyEnemyRobots, nearbyBullets)) {
			System.out.println("ranged combat");
			goTo(new Direction(here,MapAnalysis.center));
			return true;
		}
		if (targetGardenerID == -1 || !rc.canSenseRobot(targetGardenerID) || (rc.getRoundNum() % 25 == 3 && (targetLoc == null || !inGoodSpot(targetLoc)))) {
			targetGardenerID = -1;
			targetLoc = null;
			updateTargetGardener();
			if (targetGardenerID != -1) {
				//System.out.println("new target gardener: id = " + targetGardenerID);
				targetG = rc.senseRobot(targetGardenerID);
				//updateTargetLoc(nearbyTrees, targetG);
			}
		}
		if (targetGardenerID != -1) {
			targetG = rc.senseRobot(targetGardenerID);
				updateTargetLoc(nearbyTrees, targetG);
			if (targetLoc == null) {
				//System.out.println("can't find a tree, but still trying to kill gardener");
				if (here.distanceTo(targetG.location) < 2.5) {
					RangedCombat.shootSingleShot(targetG);
				}
				int test = Clock.getBytecodeNum();
				goTo(targetG.location);
				System.out.println("Used: " + (Clock.getBytecodeNum() - test));
			} else if (inGoodSpot(targetLoc)) {
				// rc.setIndicatorLine(here,targetG.location,0,0,255);
				shiftButtSlightly(targetLoc, targetG);
				System.out.println("shifting my butt");
				harassFromTree(targetG);
			} else {
				// rc.setIndicatorLine(here,targetLoc,0,0,255);
				//System.out.println("heading toward tree");
				if (here.distanceTo(targetG.location) < 2.5) {
					RangedCombat.shootSingleShot(targetG);
				}
				goTo(targetLoc);
			}
			return true;
		}
		return false;
	}

	private boolean inDanger(RobotInfo[] nearbyEnemyRobots, BulletInfo[] nearbyBullets) throws GameActionException {
		boolean inTree = false;
		if (targetLoc != null) {
			inTree = inGoodSpot(targetLoc);
			if (inTree && rc.canSenseRobot(targetGardenerID)&& rc.senseRobot(targetGardenerID).health > 39) {
				for (RobotInfo enemy : nearbyEnemyRobots) {
					if (enemy.type == RobotType.LUMBERJACK && Util.distanceSquaredTo(here, enemy.location) < 10) {
						return true;
					}
				}
			}
		}
		else if (dangerRating(here) > 0) {
			return true;
		}

		return false;
	}

	private static void updateTargetGardener() {
		RobotInfo closestEnemyGardener = closestUndefendedGardener(nearbyEnemyRobots);
		if (closestEnemyGardener != null) {
			targetGardenerID = closestEnemyGardener.ID;
		}
	}

	private static void updateTargetLoc(TreeInfo[] nearbyTrees, RobotInfo targetG) throws GameActionException {
		TreeInfo bestTree = closestSafeTree(nearbyTrees, targetG.location);
		if (bestTree != null) {
			MapLocation outerEdge = bestTree.location.add(bestTree.location.directionTo(targetG.location),
					bestTree.radius);
			targetLoc = outerEdge.add(targetG.location.directionTo(bestTree.location),(float) 1.005);
			rc.setIndicatorLine(here, targetLoc, 255, 255, 255);
			rc.setIndicatorLine(here, bestTree.location, 0, 0, 0);

		}
	}

	// note: array trees not presorted because they are combined neutral and
	// enemy
	public static TreeInfo closestSafeTree(TreeInfo[] trees, MapLocation toHere) {
		TreeInfo closest = null;
		float bestDist = 5;
		float dist;
		for (int i = trees.length; i-- > 0;) {
			dist = toHere.distanceTo(trees[i].location) + here.distanceTo(trees[i].location)/10;
			if (dist < bestDist) {
					RobotInfo[] enemiesWithinRangeOfTree = rc.senseNearbyRobots(trees[i].location, (float) 2.1, enemy);
					if (consistsOfOnlyHarmlessUnits(enemiesWithinRangeOfTree)) {
						bestDist = dist;
						closest = trees[i];
					}
			}
		}
		return closest;
	}

	private static RobotInfo closestUndefendedGardener(RobotInfo[] robots) {
		for (int i = robots.length; i-- > 0;) {
			if (robots[i].type == RobotType.GARDENER)
				return robots[i];
		}
		return null;
		/*
		 * from before things were presorted: RobotInfo closest = null; float
		 * bestDist = 99999; float dist; for (int i = robots.length; i-- > 0;) {
		 * if (robots[i].type == RobotType.GARDENER) { dist =
		 * here.distanceTo(robots[i].location); RobotInfo[]
		 * enemiesWithinRangeOfGardener =
		 * rc.senseNearbyRobots(robots[i].location, 3, enemy); if (dist <
		 * bestDist &&
		 * consistsOfOnlyHarmlessUnits(enemiesWithinRangeOfGardener)) { bestDist
		 * = dist; closest = robots[i]; } } } return closest;
		 */
	}

	private static boolean consistsOfOnlyHarmlessUnits(RobotInfo[] enemiesWithinRangeOfGardener) {
		for (RobotInfo e : enemiesWithinRangeOfGardener) {
			if (e.type == RobotType.LUMBERJACK){//(e.type != RobotType.ARCHON && e.type != RobotType.GARDENER) {
				return false;
			}
		}
		return true;
	}

	private static void shiftButtSlightly(MapLocation targetLoc, RobotInfo targetG) throws GameActionException {
		RobotInfo meanie = closestShooter();
		if (meanie != null) {
			MapLocation outerEdge = targetLoc.add(targetLoc.directionTo(targetG.location), nearbyTrees[0].radius-1);
			goTo(outerEdge.add(closestShooter().location.directionTo(targetLoc), (float) .005));
		}
	}
	private static RobotInfo closestShooter(){
		for (RobotInfo r : nearbyRobots){
			if (r.type == RobotType.SCOUT || r.type == RobotType.SOLDIER || r.type == RobotType.TANK){
				return r;
			}
		}
		return null;
	}
	/**
	 * Checks how close we are to our target tree
	 * 
	 * @param targetLoc
	 *            the tree we want to be close to
	 * @return
	 * @throws GameActionException
	 */
	private static boolean inGoodSpot(MapLocation targetLoc) throws GameActionException {
		return targetLoc.distanceTo(here) < 0.01;
	}

	/**
	 * Attempts to get in the closest tree and shoot at archons/gardeners
	 * 
	 * @param closestTarget
	 *            BodyInfo object
	 * @return true if we are in the spot we want to be in
	 * @throws GameActionException
	 */
	public static void harassFromTree(RobotInfo closestTarget) throws GameActionException {
		if (closestTarget != null) {
			// rc.setIndicatorLine(here,closestTarget.getLocation(),255,0,0);
			RangedCombat.shootSingleShot(closestTarget);
			// System.out.println("I shot");
		}
	}

	/**
	 * NOT USED Attempts to check if we have a clear shot to the target
	 * 
	 * @param closestTarget
	 *            the target we want to shoot
	 * @return true if we have a clear shot
	 */
	private static boolean haveAClearShot(RobotInfo closestTarget) throws GameActionException {
		Direction intendedAttackDir = here.directionTo(closestTarget.location);
		for (RobotInfo robot : nearbyRobots) {
			if (intendedAttackDir.radiansBetween(here.directionTo(robot.location)) < Math.PI / 10
					&& robot != closestTarget) {
				return false;
			}
		}

		TreeInfo[] nearbyTrees = Util.combineTwoTIArrays(nearbyEnemyTrees, nearbyNeutralTrees);

		for (TreeInfo tree : nearbyTrees) {
			if (intendedAttackDir.radiansBetween(here.directionTo(tree.location)) < Math.PI / 10) {
				return false;
			}
		}

		return true;
	}

}