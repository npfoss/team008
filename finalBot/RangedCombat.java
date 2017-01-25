package team008.finalBot;

import battlecode.common.*;


public class RangedCombat extends Bot {

	//Finals
	private static final String SINGLE_SHOT = "single shot";
	private static final String TRIAD_SHOT = "triad shot";
	private static final String PENTAD_SHOT = "pentad shot";
	private static final String NO_SHOT = "no shot";
	private static final float TREE_HIT_VALUE = 5 * type.attackPower;
	private static final float ENEMY_HIT_VALUE = 12 * type.attackPower;
	private static final int PENTAD_SPREAD_DEGREES = 30;
	private static final int TRIAD_SPREAD_DEGREES = 20;

	//Globals
	private static float MOVE_DIST = type.strideRadius;
	private static float safeDist = 0;
	private static boolean bulletSafe;
	private static boolean onlyHarmlessUnitsAround;
	

	/**
	 * to call execute, number of enemies must be > 0
	 */
	public static void execute() throws GameActionException {
		safeDist = 0;
	    //if(debug)System.out.println("Instantiation: "+ Clock.getBytecodeNum());
		potentialAttackStats attack = chooseTargetAndShotType();
		onlyHarmlessUnitsAround = onlyHarmlessUnitsNearby();
		//if(debug)System.out.println("Shot Calc:"+Clock.getBytecodeNum());
		if (attack == null) {
			RobotInfo bestRobot = nearbyEnemyRobots[0];
			safeDist = bestRobot.type.bodyRadius + type.bodyRadius + bestRobot.type.strideRadius
					+ (bestRobot.type == RobotType.LUMBERJACK
							? GameConstants.LUMBERJACK_STRIKE_RADIUS - bestRobot.type.bodyRadius
							: bestRobot.type.bulletSpeed);
			if (bestRobot.type == RobotType.GARDENER)
				safeDist = 0;
			if (!onlyHarmlessUnitsAround || here.distanceTo(bestRobot.location) < 3.5) {
				Direction moveDir = calcMoveDir(bestRobot);
				if (moveDir != null && rc.canMove(moveDir, MOVE_DIST))
					rc.move(moveDir, MOVE_DIST);
			} else {
				goTo(bestRobot.location);
			}

			return;
		}
		BodyInfo target = attack.getTarget();
		if (target == null) {
			// System.out.println("rip target");
			tryMoveDirection(here.directionTo(MapAnalysis.center), true, false);
			return;
		}
		//if(debug)System.out.println("My Target is"+attack.getTarget().getID());
		MapLocation targetLoc = target.getLocation();
		//Direction targetDir = here.directionTo(attack.getTarget().getLocation());
		//Direction moveDir = (targetDir);
		Direction moveDir = null;
		if(!onlyHarmlessUnitsAround){
			moveDir = calcMoveDir(attack.getTarget());
		}
        //if(debug)System.out.println("Move Calc:"+Clock.getBytecodeNum());

        if (moveDir != null || onlyHarmlessUnitsAround) {
        	if(onlyHarmlessUnitsAround && here.distanceTo(targetLoc) > 3.5){
        		goTo(targetLoc);
        	}
        	else if (moveDir != null){
				MapLocation nextLoc = here.add(moveDir, type.strideRadius);
				if (nextLoc.distanceTo(targetLoc) < here.distanceTo(targetLoc)) {
					//if(debug){System.out.println("moved before shooting");}
					rc.move(moveDir, MOVE_DIST);
	        	}
        	}
        	else{
        		moveToBinary(targetLoc);
        	}
		}

		//If we've moved recalculate the shot type but not the shot
		if(rc.hasMoved()) {
			attack.setShotType(calculateShotType(attack.getTarget(),attack.getShotValue()));
		}
        //if(debug)System.out.println("Shot recalc:"+Clock.getBytecodeNum());

        parseShotTypeAndShoot(attack.getTarget(), attack.getShotType());

		//if(debug)System.out.println("I tried to shoot a "+ attack.getShotType());
		if(rc.getMoveCount()  == 0 && moveDir != null && rc.canMove(moveDir, MOVE_DIST)){
			//System.out.println("shot before moving");
			rc.move(moveDir, MOVE_DIST);
		}
	}

	private static void moveToBinary(MapLocation t) throws GameActionException {
		Direction dir = here.directionTo(t);
		float highDist = type.strideRadius;
		float lowDist = 0;
		float midDist = (float)(0.01);
		while(highDist - lowDist > .01){
			midDist = (highDist + lowDist) / 2;
			if(rc.canMove(dir, midDist)){
				lowDist = midDist;
			}
			else{
				highDist = midDist;
			}	
		}
		rc.move(dir, (float) (midDist - .01));
	}
	
	//////////////////////////Movement Micro////////////////////////

	private static boolean onlyHarmlessUnitsNearby() {
		for(RobotInfo e: nearbyEnemyRobots){
			if(e.type != RobotType.GARDENER && e.type != RobotType.ARCHON)
				return false;
		}
		return true;
	}

	/**
	 * Picks the best direction to move in;
	 * @param target our eventual target
	 * @return the direction we want to move in
	 * @throws GameActionException 
	 */
	private static Direction calcMoveDir(BodyInfo target) throws GameActionException {
		if(onlyHarmlessUnitsAround && here.distanceTo(target.getLocation()) < 3.5)
			return null;
		MapLocation targetLoc = target.getLocation();
		if(debug)rc.setIndicatorLine(here, targetLoc, 255, 0, 0);
		Direction targetDir = here.directionTo(targetLoc);
		float maxDist = -99999;
		Direction backupDir = targetDir.opposite();
		RobotInfo[] nearbyEs = null;
		if(Math.abs(safeDist) < .01){//dealing with gardener
			nearbyEs = rc.senseNearbyRobots(targetLoc, -1, enemy);
		}
		if(Math.abs(safeDist -1) < .01){//dealing with scout in tree
			//if(debug)System.out.println("Scout Hunting");
			MapLocation targetSpot = targetLoc.add(targetDir.opposite(), (float) 2.004);
			float dist = here.distanceTo(targetSpot);
			if(dist < type.strideRadius && rc.canMove(targetDir, dist)){
				MOVE_DIST = dist;
				return targetDir;
			}
		}
		//check for easy move in desired dir
		Direction dir = targetDir;
		/*
		if(nearbyBullets.length > 0){
			Direction dirToB = here.directionTo(nearbyBullets[0].location);
			Direction dir1 = dirToB.rotateLeftDegrees(90);
			Direction dir2 = dirToB.rotateLeftDegrees(90);
			MapLocation option1 = here.add(dir1, type.strideRadius);
			MapLocation option2 = here.add(dir2, type.strideRadius);
			if(nearbyBullets.length > 1){
				float dist1 = option1.distanceTo(nearbyBullets[1].location);
				float dist2 = option2.distanceTo(nearbyBullets[1].location);
				dir = (dist1 > dist2 ? dir1: dir2);
			}
			else if(nearbyAlliedRobots.length > 0){
				float dist1 = option1.distanceTo(nearbyAlliedRobots[0].location);
				float dist2 = option2.distanceTo(nearbyAlliedRobots[0].location);
				dir = (dist1 > dist2 ? dir1: dir2);
			}
			else if(nearbyTrees.length > 0){
				float dist1 = option1.distanceTo(nearbyTrees[0].location);
				float dist2 = option2.distanceTo(nearbyTrees[0].location);
				dir = (dist1 > dist2 ? dir1: dir2);
			}
			else{
				dir = dir1;
			}
		}
		*/
		if(rc.canMove(dir, type.strideRadius)){
			MapLocation moveTo = here.add(dir, type.strideRadius);
			if(isSafe(moveTo, target, nearbyEs))
				return dir;
			else if (bulletSafe && moveTo.distanceTo(targetLoc) > maxDist){
				maxDist = moveTo.distanceTo(targetLoc);
				backupDir = dir;
			}
		}
        //if(debug)System.out.println("Checking for an easy move:"+Clock.getBytecodeNum());

        //check the other directions
		int dirsToCheck = 36;
		if(nearbyBullets.length > 5)
			dirsToCheck = 18;
		Direction left = dir.rotateLeftDegrees(360/dirsToCheck);
		Direction right = dir.rotateRightDegrees(360/dirsToCheck);
		for(int i = 0; i < dirsToCheck/2; i++){
            //if(debug)System.out.println("Going through directions:"+Clock.getBytecodeNum());

            if(rc.canMove(left, type.strideRadius)){
				MapLocation moveTo = here.add(left, type.strideRadius);
				if(isSafe(moveTo, target, nearbyEs))
					return left;
				else if (bulletSafe && moveTo.distanceTo(targetLoc) > maxDist){
					maxDist = moveTo.distanceTo(targetLoc);
					backupDir = left;
				}
			}
			if(rc.canMove(right, type.strideRadius)){
				MapLocation moveTo = here.add(right, type.strideRadius);
				if(isSafe(moveTo, target, nearbyEs))
					return right;
				else if (bulletSafe && moveTo.distanceTo(targetLoc) > maxDist){
					maxDist = moveTo.distanceTo(targetLoc);
					backupDir = right;
				}
			}
			left = left.rotateLeftDegrees(360/dirsToCheck);
			right = right.rotateRightDegrees(360/dirsToCheck);
		}
		return backupDir;
	}

	/**
	 * Checks if the loc is in danger and if it is in a safe spot relative to the target
	 * @param loc the location to check for safety
	 * @param target the eventual target whos location we want to check against
	 * @return whether that location is safe
	 */
	private static boolean isSafe(MapLocation loc, BodyInfo target, RobotInfo[] nearbyEs) {
		//System.out.println("safe dist = " + safeDist);
		float safeDistLocal = 0;

		//if(debug)System.out.println("Pre Bullet:"+Clock.getBytecodeNum());

        //check that now bullets will hit the location
		bulletSafe = true;
		for (BulletInfo b : nearbyBullets) {
		    if(b.location.distanceTo(loc) > b.speed){
		        break;
            }
			if (willCollide(b, loc)) {
				bulletSafe = false;
				return false;
			}
		}
		
		float safeLumberDist = GameConstants.LUMBERJACK_STRIKE_RADIUS + type.bodyRadius + RobotType.LUMBERJACK.strideRadius;
		
		for(RobotInfo a: nearbyRobots){
			float dist = a.location.distanceTo(loc);
			if(a.type == RobotType.LUMBERJACK && dist < safeLumberDist)
				return false;
			if(dist > safeLumberDist)
				break;
		}
	

		//if(debug)System.out.println("Post Bullet:"+Clock.getBytecodeNum());

        //check if the spot can be immediately damaged next turn
		if(Math.abs(safeDist) < .01){
            if(nearbyEnemyRobots.length == 0 || nearbyEnemyRobots.length == 1 && nearbyEnemyRobots[0].type == RobotType.GARDENER)
                return true;
			else{
					//robots closest to gardener or tree
				for(RobotInfo e: nearbyEs){
					if(e.type != RobotType.GARDENER && e.type != RobotType.ARCHON){
						loc = e.location;
						safeDistLocal = e.type.bodyRadius + type.bodyRadius + e.type.strideRadius + (e.type == RobotType.LUMBERJACK ? GameConstants.LUMBERJACK_STRIKE_RADIUS : e.type.bulletSpeed);
						if(loc.distanceTo(e.location) < safeDistLocal){
							return false;
						}
						break;
					}
				}
			}
		}

		return loc.distanceTo(target.getLocation())>safeDist;
	}



	///////////////////// Shooting and Target Micro/////////////////////

	/**
	 * Picks the target
	 * @throws GameActionException
	 */
	private static potentialAttackStats chooseTargetAndShotType() throws GameActionException {
		int score;
		boolean attackingGardener = false;
		int shotValue = 0;
		int bestScore = -999999;
		RobotInfo bestRobot = null;
		int canWeHitThemValue;
		int robotsToCalculate = 5;
		int calculated = 0;
		for (RobotInfo robot : nearbyEnemyRobots) {
			if(robot.type == RobotType.ARCHON)
				continue;
			canWeHitThemValue = canWeHitHeuristic(robot);
			score = (int) (canWeHitThemValue);
			//if(debug)System.out.println("score = " + score);
			if (score > bestScore && isDirectionSafe(robot)) {
				//if(debug) System.out.println("chose target");
				attackingGardener = (robot.type == RobotType.GARDENER);
				bestScore = score;
				bestRobot = robot;
				shotValue = canWeHitThemValue;
				if(attackingGardener){
					safeDist = 0;
				}
			}
			calculated++;
			if (calculated == robotsToCalculate){
				break;
			}
		}
		if(bestRobot != null && !attackingGardener){
			if(bestRobot.type == RobotType.SCOUT && rc.canSenseLocation(bestRobot.location) && rc.isLocationOccupiedByTree(bestRobot.location)){//edge case for scouts in trees
				safeDist = -1; //signal we are dealing with a scout
			}
			else{
				safeDist = bestRobot.type.bodyRadius + type.bodyRadius + bestRobot.type.strideRadius + (bestRobot.type == RobotType.LUMBERJACK ? GameConstants.LUMBERJACK_STRIKE_RADIUS - bestRobot.type.bodyRadius : (float)(bestRobot.type.bulletSpeed * (bestRobot.type == RobotType.SCOUT? 1 : 1.5)));//for now kinda hardcode the 1.5 -- more testing on this later
			}//System.out.println("Safe dist = " + safeDist);*/
		}
		if(bestRobot != null)
			return new potentialAttackStats(bestRobot, calculateShotType(bestRobot, shotValue), shotValue);
		return null;
	}

	/**
	 * Gives BodyInfo target a value based how likely it is we can hit them.
	 * @param robot the BodyInfo target to create an estimate for
	 * @return
	 */
	public static int canWeHitHeuristic(RobotInfo robot) {
		int score = 70;
		float howFarAwayTheyCanGet = here.distanceTo(robot.location) - type.bulletSpeed - type.bodyRadius
				- robot.type.bodyRadius + robot.type.strideRadius;
		//System.out.println("how far away they can get = " + howFarAwayTheyCanGet + " for robot at " + robot.location);
		//score -= 25 * howFarAwayTheyCanGet / (nearbyTrees.length + 1);
		score -= 5 * (howFarAwayTheyCanGet > 0 ? howFarAwayTheyCanGet: 0);
		return score;
	}

	/**
	 * Picks whether to shot 1,3 or 5 bullets.
	 * @param target the intended target for which to calculate the shot type
	 * @param singleValue the weighting created based on the can we hit heuristic
	 * @return the shot type
	 * @throws GameActionException
	 */
	public static String calculateShotType(BodyInfo target, int singleValue) throws GameActionException {
		//Cast body info if its a robot
		if(target == null)
			return NO_SHOT;
		RobotInfo targetRobot = null;
		MapLocation targetLoc = target.getLocation();
		Direction targetDir = here.directionTo(targetLoc);
		if(target.isRobot()){
			targetRobot = (RobotInfo)target;
		}
		boolean ableToShootTriad = true;
		boolean ableToShootPentad = true;
		Direction leftTriadDir = targetDir.rotateLeftDegrees(20);
		Direction rightTriadDir = targetDir.rotateRightDegrees(20);
		Direction leftPentadDir = targetDir.rotateLeftDegrees(30);
		Direction rightPentadDir = targetDir.rotateRightDegrees(30);
		int tempSV = singleValue;
		
		if(targetRobot != null && here.distanceTo(targetLoc) - type.bodyRadius - targetRobot.type.bodyRadius < type.bulletSpeed){
			if(willHitLoc(leftPentadDir, targetLoc, targetRobot.type.bodyRadius)){
				if(debug)System.out.println("close enough to pentad");
				return PENTAD_SHOT;
			}
			if(willHitLoc(leftTriadDir, targetLoc, targetRobot.type.bodyRadius)){
				if(debug)System.out.println("close enough to triad");
				return TRIAD_SHOT;
			}
		}
		
		for(RobotInfo a: nearbyAlliedRobots){
			if(a.type == RobotType.LUMBERJACK){
				singleValue = tempSV;
				break;
			}
			if(a.type == RobotType.SOLDIER || a.type == RobotType.TANK){
				singleValue += 5;
			}
		}
		
		int pentadValue = singleValue;
		int triadValue = singleValue;
		/*
		int limit = 5;
		int count = 0;
		for(TreeInfo t: nearbyNeutralTrees){
			count++;
			if(count > limit)
				break;
			if(targetRobot != null && here.distanceTo(targetRobot.location) < here.distanceTo(t.location))
				break;
			if(willHitLoc(leftTriadDir, t.location, t.radius) || willHitLoc(rightTriadDir, t.location, t.radius)){
				//if(debug)System.out.println("ruling out triad");
				triadValue -= 10;
				pentadValue -= 10;
				continue;
			}
			if(willHitLoc(leftPentadDir, t.location, t.radius) || willHitLoc(rightPentadDir, t.location, t.radius)){
				//if(debug)System.out.println("ruling out triad and pentad");
				pentadValue -= 10;
				continue;
			}
		}*/
		// come up with some sort of formula for choosing the kind of shot

        //System.out.println("singleValue = " + singleValue);

		//Its better if we can also do collateral dmg to enemy trees
		/*
		Direction targetDir = here.directionTo(target.getLocation());
		for(TreeInfo t: nearbyEnemyTrees){
			Direction dirToT = here.directionTo(t.location);
			float deg = Math.abs(targetDir.degreesBetween(dirToT));
			if(deg < PENTAD_SPREAD_DEGREES){
				if(debug)System.out.println("added enemy tree to pentad");
				pentadValue += TREE_HIT_VALUE;
				if(deg < TRIAD_SPREAD_DEGREES){
					triadValue += TREE_HIT_VALUE;
				}
			}
		}*/
		
		//Its better if we can deal collateral dmg to other enemies
		for(RobotInfo r: nearbyEnemyRobots){
			if(!ableToShootTriad){
				break;
			}
			if(r == targetRobot){
				//if(debug)System.out.println("didn't count target");
				continue;
			}
			Direction dirToR = here.directionTo(r.location);
			//if(debug) System.out.println("target Dir = " + targetDir);
			//if(debug) System.out.println("dirToR = " + dirToR);
			float deg = Math.abs(targetDir.degreesBetween(dirToR));
			//if(debug) System.out.println("deg = " + deg);
			if(deg < PENTAD_SPREAD_DEGREES + 5){
				float howFarAwayTheyCanGet = here.distanceTo(r.location)
						- type.bulletSpeed
						- type.bodyRadius
						- r.type.bodyRadius
						+ r.type.strideRadius;

				float valFromHitting = ENEMY_HIT_VALUE - howFarAwayTheyCanGet; 
				if(valFromHitting < 0)
					continue;
				//if(debug)System.out.println("adding robot at " + r.location);
				if(ableToShootPentad){
					pentadValue += valFromHitting;
				}
				if(deg < TRIAD_SPREAD_DEGREES + 5){
					triadValue += valFromHitting;
				}
			}
		}

		//adjusting shots
		/*
		if(pentadValue == triadValue && pentadValue > 110){
			pentadValue = -1;
			triadValue = 111;
		}*/
		if(target.isRobot() && targetRobot!=null && targetRobot.type == RobotType.ARCHON){
			return SINGLE_SHOT;
		}

		if(debug)System.out.println("Pentad Value: " + pentadValue);
		if(debug)System.out.println("Triad Value: " + triadValue);
		/*if (target != null) {
			if (here.distanceTo(target.getLocation()) - type.bulletSpeed - type.bodyRadius - target.getRadius() < 0) {
				score = 7;
			}
		}*/
		int treeMod = rc.getTreeCount() / 3;
		if (ableToShootPentad && pentadValue + treeMod + (type.attackPower + type.bulletSpeed) * 4 > 126 && (pentadValue > 150 || pentadValue > triadValue)) {
			return PENTAD_SHOT;
		}
		float twoTurnsMove = targetRobot.type.strideRadius * 2;
		float twoTurnsParallelToShot = (float)(Math.sqrt(twoTurnsMove*twoTurnsMove - targetRobot.type.bodyRadius * targetRobot.type.bodyRadius));
		float threeTurnsMove = targetRobot.type.strideRadius * 3;
		float threeTurnsParallelToShot = (float)(Math.sqrt(threeTurnsMove*threeTurnsMove - targetRobot.type.bodyRadius * targetRobot.type.bodyRadius));
		float targetDist = here.distanceTo(target.getLocation());
		float twoDistTheyCanGetAway = targetDist + twoTurnsParallelToShot - type.bodyRadius - targetRobot.type.bodyRadius;
		float threeDistTheyCanGetAway = targetDist + threeTurnsParallelToShot - type.bodyRadius - targetRobot.type.bodyRadius;
		if(type == RobotType.TANK){
			if(type.bulletSpeed * 2 > twoDistTheyCanGetAway && twoDistTheyCanGetAway * Math.tan(Math.PI/12) < targetRobot.type.bodyRadius * 2 + 0.5)
				return PENTAD_SHOT;
			if(type.bulletSpeed * 3 > threeDistTheyCanGetAway && threeDistTheyCanGetAway * Math.tan(Math.PI/12) < targetRobot.type.bodyRadius * 2 + 0.5)
				return PENTAD_SHOT;
		}
		if (ableToShootTriad && triadValue + treeMod + (type.attackPower + type.bulletSpeed) * 4 > 111) {
			return TRIAD_SHOT;
		}
		if(type == RobotType.SOLDIER){
			if(debug)System.out.println(threeDistTheyCanGetAway);
			if(type.bulletSpeed * 2 > twoDistTheyCanGetAway && twoDistTheyCanGetAway * Math.tan(Math.PI/9) < targetRobot.type.bodyRadius * 2 + 0.5)
				return TRIAD_SHOT;
			if(type.bulletSpeed * 3 > threeDistTheyCanGetAway && threeDistTheyCanGetAway * Math.tan(Math.PI/9) < targetRobot.type.bodyRadius * 2 + 0.5)
				return TRIAD_SHOT;
		}
		if (singleValue + treeMod + (type.attackPower + type.bulletSpeed) * 4 > 85){
			return SINGLE_SHOT;
		}
		return NO_SHOT;

	}

	/**
	 * Parses the shot type and fires
	 * @param target the intended BodyInfo to shoot at
	 * @param shotType the intended type of shot
	 * @throws GameActionException
	 */
	public static void parseShotTypeAndShoot(BodyInfo target, String shotType) throws GameActionException {
		switch (shotType) {
			case SINGLE_SHOT:
				shootSingleShot(target);
				break;
			case TRIAD_SHOT:
				shootTriadShot(target);
				break;
			case PENTAD_SHOT:
				shootPentadShot(target);
				break;
			default: // do nothing, it isn't worth shooting.
		}
	}

	/**
	 * Determines if shooting at a target will cause friendly fire.
	 *
	 * @param target
	 * @return true if it could also hit a friend
	 * @throws GameActionException
	 */
	private static boolean isDirectionSafe(RobotInfo target) throws GameActionException {
		//if(debug)System.out.println("starting isDirSafe " + Clock.getBytecodeNum());
		Direction intendedAttackDir = here.directionTo(target.location);
		for (RobotInfo friend : nearbyAlliedRobots) {
			if (friend.location.distanceTo(here) < here.distanceTo(target.location) - type.bodyRadius
					- target.type.bodyRadius){ 
				if (willHitLoc(intendedAttackDir, friend.location, friend.type.bodyRadius)) {
					//if(debug)System.out.println("Direction is not safe");
					return false;
				}
			} else {
				break;
			}

		}
		for (TreeInfo friend : nearbyTrees) {
			if (friend.location.distanceTo(here) - friend.radius - target.type.strideRadius < here.distanceTo(target.location)) {
				if (willHitLoc(intendedAttackDir, friend.location, friend.radius)) {
					//if(debug)System.out.println("Direction is not safe");
					return false;
				}
			} else {
				break;
			}
		}
		//if(debug)System.out.println("end dir safe " + Clock.getBytecodeNum());
//		for (TreeInfo friend : nearbyNeutralTrees) {
//			if (friend.location.distanceTo(here) < here.distanceTo(target.location) - type.bodyRadius
//					- target.type.bodyRadius) {
//				if (intendedAttackDir.radiansBetween(here.directionTo(friend.location)) < Math.PI / 12) {
//					return false;
//				}
//			} else {
//				break;
//			}
//		}
		return true;
	}


	public static boolean willHitLoc(Direction dir, MapLocation loc, float rad){
		MapLocation leftSide = loc.add(here.directionTo(loc).rotateLeftDegrees(90), rad);
		MapLocation rightSide = loc.add(here.directionTo(loc).rotateRightDegrees(90), rad);
		Direction left = here.directionTo(leftSide);
		Direction right = here.directionTo(rightSide);
		float degL = Math.abs(left.degreesBetween(dir));
		float degR = Math.abs(right.degreesBetween(dir));
		float degT = Math.abs(left.degreesBetween(right));
		if (degL < degT && degR < degT) {
			return true;
		}
		return false;
	}
	
	///////////////////// These Might Belong in Util/////////////////////
	public static void shootSingleShot(BodyInfo target) throws GameActionException {
		if (rc.canFireSingleShot() && target != null) {
			rc.fireSingleShot(rc.getLocation().directionTo(target.getLocation()));
			//System.out.println("I shot a single");

		}

	}

	public static void shootTriadShot(BodyInfo target) throws GameActionException {
		//System.out.println(target == null);

		if (rc.canFireTriadShot() && target != null) {
			rc.fireTriadShot(rc.getLocation().directionTo(target.getLocation()));
			//System.out.println("I shot a triad");

		}

	}

	public static void shootPentadShot(BodyInfo target) throws GameActionException {
		//System.out.println(target == null);

		if (rc.canFirePentadShot() && target != null) {
			rc.firePentadShot(rc.getLocation().directionTo(target.getLocation()));
			//System.out.println("I shot a pentad");

		}

	}

	/////////// class
	private static class potentialAttackStats {
		BodyInfo target;
		String shotType;
		int shotValue;
		//boolean attackingGardener;

		public potentialAttackStats(BodyInfo target, String shotType, int shotValue/*, boolean attackingGardener*/) {
			this.target = target;
			this.shotType = shotType;
			this.shotValue = shotValue;
			//this.attackingGardener = attackingGardener;
		}

		public BodyInfo getTarget() {
			return target;
		}

		public void setTarget(BodyInfo target) {
			this.target = target;
		}

		public String getShotType() {
			return shotType;
		}

		public void setShotType(String shotType) {
			this.shotType = shotType;
		}

		public int getShotValue() {
			return shotValue;
		}

		public void setShotValue(int shotValue) {
			this.shotValue = shotValue;
		}
		/*
		public boolean getAttackingGardener() {
			return attackingGardener;
		}

		public void setAttackingGardener(boolean attackingGardener) {
			this.attackingGardener = attackingGardener;
		}*/
	}
}