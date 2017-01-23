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
	private static boolean bulletSafe;

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
		/*
		if (!tryToHarass(nearbyTrees)) {
			if (!dealWithNearbyTrees()) {
				moveToHarass();
			}
		}*/
		
		if (!dealWithNearbyTrees()){
			explore();
		}

		// rc.setIndicatorDot(here,0,255,0);
		if (nearbyEnemyRobots.length > 0 && (rc.getRoundNum() + rc.getID()) % 25 == 0) {
			// rc.setIndicatorDot(enemies[0].location, 255, 0, 0);
			notifyFriendsOfEnemies(nearbyEnemyRobots);
		}
		
		if(Clock.getBytecodesLeft() > 3000 && searchingForEdges){
			searchForEdges();
		}
		return;
	}

	private void searchForEdges() throws GameActionException {
		//System.out.println("searching");
		//System.out.println(Math.abs(maxY));
		if (!foundMinX) {
			if (rc.readBroadcast(17) == 1) {
				foundMinX = true;
			} else {
				MapLocation edge = checkForEdge(Direction.getWest());
				if (edge != null) {
					Message.MIN_X.setValue(edge.x);
					foundMinX = true;
					rc.broadcast(17, 1);
					// System.out.println("updated min x to " + minX);
				}
			}
		}
		if(!foundMaxX){
			if (rc.readBroadcast(18) == 1) {
				foundMaxX = true;
			} 
			else{
				MapLocation edge = checkForEdge(Direction.getEast());
				if(edge != null){
					Message.MAX_X.setValue(edge.x);
					foundMaxX = true;
					rc.broadcast(18, 1);
					//System.out.println("updated max x to " + maxX);
				}
			}
		}
		if(!foundMinY){
			if (rc.readBroadcast(19) == 1) {
				foundMinY = true;
			} 
			else{
				MapLocation edge = checkForEdge(Direction.getSouth());
				if(edge != null){
					Message.MIN_Y.setValue(edge.y);
					foundMinY = true;
					rc.broadcast(19, 1);
					//System.out.println("updated min y to " + minY);
				}
			}
		}
		if(!foundMaxY){
			if (rc.readBroadcast(20) == 1) {
				foundMaxY = true;
			} 
			else{
				MapLocation edge = checkForEdge(Direction.getNorth());
				if(edge != null){
					Message.MAX_Y.setValue(edge.y);
					foundMaxY = true;
					rc.broadcast(20, 1);
					//System.out.println("updated max y to " + maxY);
				}
			}
		}
		if(foundMinX && foundMinY && foundMaxY && foundMaxX){
			searchingForEdges = false;
			if(Message.MAP_SIZE.getValue() == 0){
				float area = (Message.MAX_X.getFloatValue() - Message.MIN_X.getFloatValue()) * (Message.MAX_Y.getFloatValue() - Message.MIN_Y.getFloatValue());
				Message.MAP_SIZE.setValue((int)area);
			}
			//System.out.println("area = " + area);
		}
	}

	private MapLocation checkForEdge(Direction dir) throws GameActionException {
		float highDist = type.sensorRadius - type.bodyRadius * 2;
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
		//int start = Clock.getBytecodeNum();
		for (TreeInfo tree : nearbyNeutralTrees) {
			if (tree.containedBullets > 0) {
				goTo(tree.location);
				return true;
			}
		}
		return false;
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
		/*
		update target gardener
		harass
		*/
		RobotInfo targetG = null;
		RobotInfo closestEnemy = closestHarmfulEnemy();
		if (targetGardenerID == -1 || !rc.canSenseRobot(targetGardenerID) || (rc.getRoundNum() % 35 == 3 && (targetLoc == null || !inGoodSpot(targetLoc)))) {
			targetGardenerID = -1;
			targetLoc = null;
			updateTargetGardener(closestEnemy);
			if (targetGardenerID != -1) {
				if(debug)System.out.println("new target gardener: id = " + targetGardenerID);
				targetG = rc.senseRobot(targetGardenerID);
				//updateTargetLoc(nearbyTrees, targetG);
			}
		}
		if (targetGardenerID != -1) {
			targetG = rc.senseRobot(targetGardenerID);
			//if(targetLoc != null)
			//System.out.println("my target location is " + targetLoc.toString());
			if(targetLoc != null && debug) rc.setIndicatorLine(here, targetLoc, 255, 0, 0);
			if (targetLoc == null) {
				// System.out.println("can't find a tree, but still trying to
				// kill gardener");
				if(closestEnemy == null){
					goToDangerous(targetG.location);
				}
				else{
					harassMove(targetG.location, closestEnemy);
				}
				if (here.distanceTo(targetG.location) < 2.005) {
					RangedCombat.shootSingleShot(targetG);
				}
			} else if (inGoodSpot(targetLoc)) {
				// rc.setIndicatorLine(here,targetG.location,0,0,255);
				if(closestEnemy == null)
					harassFromTree(targetG);
				else if(closestEnemy.type != RobotType.LUMBERJACK){
					shiftButtSlightly(targetLoc, targetG);
					if(debug)System.out.println("shifting my butt");
					harassFromTree(targetG);
				}
				else if(here.distanceTo(closestEnemy.location) <  type.bodyRadius + RobotType.LUMBERJACK.strideRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS){
					updateTargetLoc(nearbyTrees, targetG, closestEnemy);
					harassMove(targetG.location, closestEnemy);
				}
				else{
					harassFromTree(targetG);
				}
			} else {
				// rc.setIndicatorLine(here,targetLoc,0,0,255);
				// System.out.println("heading toward tree");
				if (here.distanceTo(targetG.location) < 2.005) {
					RangedCombat.shootSingleShot(targetG);
				}
				if(closestEnemy == null){
					goToDangerous(targetG.location);
				}
				else{
					harassMove(targetG.location, closestEnemy);
				}
				if (here.distanceTo(targetG.location) < 2.005 && rc.canFireSingleShot()) {
					RangedCombat.shootSingleShot(targetG);
				}
			}
			return true;
		}
		return false;
	}

	private void harassMove(MapLocation targetG, RobotInfo closestEnemy) throws GameActionException {
		float safeDist = closestEnemy.type.bodyRadius + type.bodyRadius + closestEnemy.type.strideRadius + (closestEnemy.type == RobotType.LUMBERJACK ? GameConstants.LUMBERJACK_STRIKE_RADIUS - closestEnemy.type.bodyRadius : closestEnemy.type.bulletSpeed);
		MapLocation enemyLoc = closestEnemy.location;
		MapLocation target = targetLoc;
		if(target == null){
			target = targetG;
		}
		Direction targetDir = here.directionTo(target);
		float dist = here.distanceTo(target);
		if(dist < type.strideRadius && rc.canMove(targetDir, dist) && isSafe(here.add(targetDir, dist), closestEnemy, safeDist)){
			rc.move(targetDir, dist);
			return;
		}
		float maxDist = -99999;
		Direction backupDir = null;
		//check for easy move in desired dir
		Direction dir = here.directionTo(target);
		if(rc.canMove(dir, type.strideRadius)){
			MapLocation moveTo = here.add(dir, type.strideRadius);
			if(isSafe(moveTo, closestEnemy, safeDist)){
				rc.move(dir, type.strideRadius);
				return;
			}
			else if (bulletSafe && moveTo.distanceTo(enemyLoc) > maxDist){
				maxDist = moveTo.distanceTo(enemyLoc);
				backupDir = dir;
			}
		}
        if(debug)System.out.println("Checking for an easy move:"+Clock.getBytecodeNum());

        //check the other directions
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for(int i = 0; i < 18; i++){
            if(debug)System.out.println("Going through directions:"+Clock.getBytecodeNum());

            if(rc.canMove(left, type.strideRadius)){
				MapLocation moveTo = here.add(left, type.strideRadius);
				if(isSafe(moveTo, closestEnemy, safeDist)){
					rc.move(left, type.strideRadius);
					return;
				}
				else if (bulletSafe && moveTo.distanceTo(enemyLoc) > maxDist){
					maxDist = moveTo.distanceTo(enemyLoc);
					backupDir = left;
				}
			}
			if(rc.canMove(right, type.strideRadius)){
				MapLocation moveTo = here.add(right, type.strideRadius);
				if(isSafe(moveTo, closestEnemy, safeDist)){
					rc.move(right, type.strideRadius);
					return;
				}
				else if (bulletSafe && moveTo.distanceTo(enemyLoc) > maxDist){
					maxDist = moveTo.distanceTo(enemyLoc);
					backupDir = right;
				}
			}
			left = left.rotateLeftDegrees(10);
			right = right.rotateRightDegrees(10);
		}
		if(rc.canMove(backupDir, type.strideRadius))
			rc.move(backupDir, type.strideRadius);
	}

	/**
	 * Checks if the loc is in danger and if it is in a safe spot relative to the target
	 * @param loc the location to check for safety
	 //* @param target the eventual target whos location we want to check against
	 * @return whether that location is safe
	 * @throws GameActionException 
	 */
	private static boolean isSafe(MapLocation loc, RobotInfo closestEnemy, float safeDist) throws GameActionException {
		MapLocation enemyLoc = closestEnemy.location;
		if(targetLoc != null && closestEnemy.type != RobotType.LUMBERJACK && loc == targetLoc && rc.isLocationOccupiedByTree(loc))//hide in dat tree
			return true;
		//System.out.println("safe dist = " + safeDist);

        if(debug)System.out.println("Pre Bullet:"+Clock.getBytecodeNum());

        //check that now bullets will hit the location
		bulletSafe = true;
		for (BulletInfo b : nearbyBullets) {
		    if(b.location.distanceTo(loc) > 3){
		        break;
            }
			if (willCollide(b, loc)) {
				bulletSafe = false;
				return false;
			}
		}

		if(debug)System.out.println("Post Bullet:"+Clock.getBytecodeNum());

        //check if the spot can be immediately damaged next turn
		
		return loc.distanceTo(enemyLoc) > safeDist;
	}

	private RobotInfo closestHarmfulEnemy() {
		RobotInfo e = null;
		for(RobotInfo ri: nearbyEnemyRobots){
			if(ri.type != RobotType.GARDENER && ri.type != RobotType.ARCHON){
				e = ri;
				break;
			}
		}
		return e;
	}

	private static void updateTargetGardener(RobotInfo closestEnemy) {
		RobotInfo closestEnemyGardener = bestTargetGardener(closestEnemy);
		if (closestEnemyGardener != null) {
			targetGardenerID = closestEnemyGardener.ID;
		}
	}

	private static void updateTargetLoc(TreeInfo[] nearbyTrees, RobotInfo targetG, RobotInfo closestEnemy) throws GameActionException {
		TreeInfo bestTree = null;
		if(closestEnemy == null)
			bestTree = closestTree(nearbyTrees, targetG.location);
		else{
			bestTree = calcBestTree(nearbyTrees, targetG.location, closestEnemy);
		}
		if (bestTree != null) {
			MapLocation outerEdge = bestTree.location.add(bestTree.location.directionTo(targetG.location),
					bestTree.radius);
			targetLoc = outerEdge.add(targetG.location.directionTo(bestTree.location),(float) 1.000);
			//System.out.println("my just updated target location is " + targetLoc.toString());
		}
		else{
			targetLoc = null;
		}
	}

	private static TreeInfo calcBestTree(TreeInfo[] trees, MapLocation toHere, RobotInfo closestEnemy) {
		TreeInfo best = null;
		float bestDist = 5;
		float dist;
		Direction bestDir = closestEnemy.location.directionTo(toHere);
		float bestDif = 181;
		float dif;
		for (int i = trees.length; i-- > 0;) {
			dist = toHere.distanceTo(trees[i].location) - trees[i].radius + here.distanceTo(trees[i].location)/10;
			if (dist < bestDist + 0.5) {
					Direction dir = toHere.directionTo(trees[i].location);
					dif = dir.radiansBetween(bestDir);
					if(dif < bestDif){
						bestDif = dif;
						bestDist = dist;
						best = trees[i];
					}	
			}
		}
		return best;
	}

	// note: array trees not presorted because they are combined neutral and
	// enemy
	public static TreeInfo closestTree(TreeInfo[] trees, MapLocation toHere) {
		TreeInfo closest = null;
		float bestDist = 5;
		float dist;
		for (int i = trees.length; i-- > 0;) {
			dist = toHere.distanceTo(trees[i].location) - trees[i].radius + here.distanceTo(trees[i].location)/10;
			if (dist < bestDist) {
					bestDist = dist;
					closest = trees[i];
			}
		}
		return closest;
	}

	private static RobotInfo bestTargetGardener(RobotInfo closestEnemy) {
		float maxDist = -1;
		RobotInfo ret = null;
		for (int i = nearbyEnemyRobots.length; i-- > 0;) {
			if (nearbyEnemyRobots[i].type == RobotType.GARDENER){
				if(closestEnemy == null)
					return nearbyEnemyRobots[i];
				else if (nearbyEnemyRobots[i].location.distanceTo(closestEnemy.location) > maxDist){
					ret = nearbyEnemyRobots[i];
					maxDist = nearbyEnemyRobots[i].location.distanceTo(closestEnemy.location);
				}
					
			}
		}
		return ret;
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
			goToDangerous(outerEdge.add(closestShooter().location.directionTo(targetLoc), (float) .001));
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

}