package team008.finalBot;

import battlecode.common.*;

public class RangedCombat extends Bot {

	//Finals
	private static final String SINGLE_SHOT = "single shot";
	private static final String TRIAD_SHOT = "triad shot";
	private static final String PENTAD_SHOT = "pentad shot";
	private static final String NO_SHOT = "no shot";
	private static final float TREE_HIT_VALUE = 5 * type.attackPower;
	private static final float ENEMY_HIT_VALUE = 10 * type.attackPower;
	private static final int PENTAD_SPREAD_DEGREES = 30;
	private static final int TRIAD_SPREAD_DEGREES = 20;

	//Globals
	private static int blockedByTree = 0;
	private static float MOVE_DIST = type.strideRadius;
	private static float safeDist = 0;
	private static boolean bulletSafe;
	private static boolean onlyHarmlessUnitsAround;
	public static BulletInfo bulletTarget;
	

	/**
	 * to call execute, number of enemies must be > 0
	 */
	public static void execute() throws GameActionException {
		blockedByTree = 0;
		//if(debug)System.out.println("here");
	    //if(debug)System.out.println("Instantiation: "+ Clock.getBytecodeNum());
		potentialAttackStats attack = chooseTargetAndShotType();
		onlyHarmlessUnitsAround = onlyHarmlessUnitsNearby();
		MapLocation targetLoc;
		//if(debug)System.out.println("Shot Calc:"+Clock.getBytecodeNum());
		if (attack == null) {
			RobotInfo closestRobot = nearbyEnemyRobots[0];
			targetLoc = closestRobot.location;
			safeDist = calcSafeDist(nearbyEnemyRobots[0]);
			RobotInfo tG = Util.closestSpecificType(nearbyAlliedRobots, here, RobotType.GARDENER);
			if(safeDist == -1){
        		Direction dir = targetLoc.directionTo(here);
        		if(tG != null){
        			dir = tG.location.directionTo(targetLoc);
        		}
        		targetLoc = targetLoc.add(dir, (float) 2.0001);
        		if(here.distanceTo(targetLoc) > 0.5)
        			goTo(targetLoc);
        		else
        			moveToBinary(targetLoc);
        		//rc.setIndicatorLine(here, targetLoc, 255, 0, 0);
        	}
			else if(tG != null){
				goTo(tG.location);
			}
			else if (!onlyHarmlessUnitsAround || here.distanceTo(closestRobot.location) < 3.5) {
				Direction moveDir = calcMoveDir(closestRobot);
				if (moveDir != null && rc.canMove(moveDir, MOVE_DIST))
					rc.move(moveDir, MOVE_DIST);
			} else if(nearbyBullets.length > 0){ 
				bulletMove(nearbyEnemyRobots[0].location, true);
			}
			else {
				goTo(closestRobot.location);
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
		targetLoc = target.getLocation();
		//Direction targetDir = here.directionTo(attack.getTarget().getLocation());
		//Direction moveDir = (targetDir);
		Direction moveDir = null;
		if(!onlyHarmlessUnitsAround){
			moveDir = calcMoveDir(attack.getTarget());
		}
        //if(debug)System.out.println("Move Calc:"+Clock.getBytecodeNum());

        if (moveDir != null || onlyHarmlessUnitsAround) {
			RobotInfo tG = Util.closestSpecificType(nearbyAlliedRobots, here, RobotType.GARDENER);
        	if(safeDist == -1){
        		Direction dir = targetLoc.directionTo(here);
        		if(tG != null){
        			dir = tG.location.directionTo(targetLoc);
        		}
        		targetLoc = targetLoc.add(dir, (float) 2.0001);
        		if(here.distanceTo(targetLoc) > 0.5)
        			goTo(targetLoc);
        		else
        			moveToBinary(targetLoc);
        		//rc.setIndicatorLine(here, targetLoc, 255, 0, 0);
        	}
        	else if(tG != null){
        		goTo(tG.location);
        	}
        	else if(onlyHarmlessUnitsAround && (here.distanceTo(targetLoc) > 3.5 || nearbyEnemyRobots[0].type == RobotType.ARCHON && nearbyEnemyRobots.length == 1)){
        		goTo(targetLoc);
        	}
        	else if (moveDir != null){
				MapLocation nextLoc = here.add(moveDir, type.strideRadius);
				if (nextLoc.distanceTo(targetLoc) < here.distanceTo(targetLoc)) {
					//if(debug){System.out.println("moved before shooting");}
					rc.move(moveDir, MOVE_DIST);
	        	}
        	}
        	else if (onlyHarmlessUnitsAround){
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
		float midDist = (float)(type.strideRadius/2);
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

	public static boolean onlyHarmlessUnitsNearby() {
		for(RobotInfo e: nearbyEnemyRobots){
			if(e.type != RobotType.GARDENER && e.type != RobotType.ARCHON && e.type != RobotType.SCOUT)
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
		if(nearbyBullets.length > 0){
			return bulletMove(targetLoc, false);
		}
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
            //if(debug)System.out.println("Going through directions:"+Clock.getBytecodeNum() );

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
	
	public static Direction bulletMove(MapLocation targetLoc, boolean makeMove) throws GameActionException {
		if(rc.canSenseLocation(targetLoc)){
			RobotInfo targetRobot = rc.senseRobotAtLocation(targetLoc);
			if(targetRobot != null && targetRobot.team == enemy && type != RobotType.LUMBERJACK){
				if((targetRobot.type == RobotType.ARCHON || targetRobot.type == RobotType.GARDENER) && nearbyEnemyRobots.length > 1)
					targetRobot = nearbyEnemyRobots[1];
				safeDist = calcSafeDist(targetRobot);
			}
			else{
				safeDist = 0;
			}
		}
		//if(debug)System.out.println("safeDist = " + safeDist);
		//if(debug)System.out.println("bytecodes = " + Clock.getBytecodeNum() + " at start.");
		//int precision = 5;
		int dirsToCheck = 12;
		int score = 0;
		for(BulletInfo b: nearbyBullets){
			float dist = here.distanceTo(b.location);
			if(dist > b.speed * 2 - type.strideRadius + type.bodyRadius){
				break;
			}
			if(willCollide(b, here)){
				score += b.damage;
			}
			else{
				score += b.damage * willKillMeNextTurn(b, here);
			}
		}
		float bestScore = score;
		Direction bestDir = null;
		float bestDist = here.distanceTo(targetLoc);
		Direction dir = here.directionTo(nearbyBullets[0].location).opposite();
		int count;
		int limit = 5;
		for(int i = 0; i < dirsToCheck; i++){
			float moveDist = type.strideRadius;
			if(!rc.canMove(dir, moveDist)){
				dir = dir.rotateLeftDegrees(360/dirsToCheck);
				continue;
			}
			score = 0;
			MapLocation moveTo = here.add(dir, moveDist);
			count = 0;
			for(BulletInfo b: nearbyBullets){
				//rc.setIndicatorLine(here, b.location, 255, 0, 0);
				count++;
				float dist = here.distanceTo(b.location);
				if(Clock.getBytecodesLeft() < 1000){
					score = 9999;
					break;
				}
				if(count > limit || dist > b.speed * 2 - type.strideRadius + type.bodyRadius){
					break;
				}
				//System.out.println("checking bullet at " + b.location);
				if(willCollide(b, moveTo)){
					score += b.damage;
					if(score > bestScore)
						break;
				}
				else{
					score += b.damage * willKillMeNextTurn(b, moveTo);
					if(score > bestScore)
						break;
				}
			}
			if(score < bestScore){
				bestScore = score;
				score = i;
				bestDist = moveTo.distanceTo(targetLoc);
				bestDir = dir;
			}
			else if(score == bestScore){
				float dist = moveTo.distanceTo(targetLoc);
				if(bestDist > safeDist && dist > safeDist ? dist < bestDist: dist > bestDist){
					bestDist = dist;
					bestDir = dir;
				}
			}
			if(Clock.getBytecodesLeft() < 2500)
				break;
			dir = dir.rotateLeftDegrees(360/dirsToCheck);
		}
		//if(debug)System.out.println("bytecodes = " + Clock.getBytecodeNum() + " at end");
		//if(debug)System.out.println("chose direction " + bestDir + " with score of " + bestScore + " and dist of " + bestDist);
		if(makeMove && bestDir != null){
			rc.move(bestDir, type.strideRadius);
		}
		return bestDir;
	}

	private static float willKillMeNextTurn(BulletInfo b, MapLocation loc) {
		//System.out.println("here");
		MapLocation nextBulletLoc = b.location.add(b.dir, b.speed);
		Direction directionToRobot = nextBulletLoc.directionTo(loc);
		float theta = Math.abs(b.dir.radiansBetween(directionToRobot));
		if (theta > Math.PI / 2) {
			return 0;
		}
		float distToRobot = nextBulletLoc.distanceTo(loc);
		float perpendicularDist = (float) (distToRobot * Math.sin(theta));
		//System.out.println("distToRobot = " + distToRobot);
		//System.out.println("perpDist = " + perpendicularDist);
		if(distToRobot - type.bodyRadius + type.strideRadius < b.speed * 2 + .01){
			if(perpendicularDist < type.bodyRadius - type.strideRadius + .01){
				return 1;
			}
			if(perpendicularDist < type.bodyRadius){
				//limited options
				return (float)((type.bodyRadius - perpendicularDist) / type.bodyRadius);
			}
		}
		return 0;
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
		/*
		bulletSafe = true;
		for (BulletInfo b : nearbyBullets) {
		    if(b.location.distanceTo(loc) > b.speed){
		        break;
            }
			if (willCollide(b, loc)) {
				bulletSafe = false;
				return false;
			}
		}*/
		
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
		boolean attackingHarmlessUnit = false;
		int shotValue = 0;
		int bestScore = -999999;
		RobotInfo bestRobot = null;
		int canWeHitThemValue;
		int robotsToCalculate = 3;
		int calculated = 0;
		for (int i = 0; i < nearbyEnemyRobots.length; i++) {
			RobotInfo robot = nearbyEnemyRobots[i];
			if(robot.health < robot.type.maxHealth && robotsToCalculate > i + 1){
				RobotInfo[] nearbyEnemiesToThatRobot = rc.senseNearbyRobots(robot.location, 2, enemy);
				if(nearbyEnemiesToThatRobot.length > 1 && nearbyEnemiesToThatRobot[1].type == RobotType.GARDENER){
					continue;
				}
			}
			if(robot.type == RobotType.ARCHON && nearbyEnemyRobots.length > robotsToCalculate + 1)
				continue;
			if(robot.type == RobotType.SCOUT && nearbyEnemyRobots.length > robotsToCalculate + 1)
				continue;
			canWeHitThemValue = canWeHitHeuristic(robot);
			score = (int) (canWeHitThemValue);
			//if(debug)System.out.println("score = " + score);
			if (score > bestScore && isDirectionSafe(robot)) {
				//if(debug) System.out.println("chose target");
				attackingHarmlessUnit = (robot.type == RobotType.GARDENER || robot.type == RobotType.ARCHON);
				bestScore = score;
				bestRobot = robot;
				shotValue = canWeHitThemValue;
				if(attackingHarmlessUnit){
					safeDist = 0;
				}
			}
			calculated++;
			if (calculated == robotsToCalculate){
				break;
			}
		}
		if(bestRobot != null && !attackingHarmlessUnit){
				safeDist = calcSafeDist(bestRobot);
		}
		if(bestRobot != null)
			return new potentialAttackStats(bestRobot, calculateShotType(bestRobot, shotValue), shotValue);

        return null;
	}

    /**
     * Hopefully this mean we're getting shot at but might not see them
     * @return
     */
    public static int numBulletsEnemyShot() {
        int count = 0;
	    if(nearbyBullets.length > 10){
	        for(BulletInfo bullet: nearbyBullets){
                if((Math.abs(bullet.getLocation().directionTo(here).radiansBetween(bullet.getDir()))) < Math.PI/6){
	                count++;
	                bulletTarget = bullet;
                }
            }
        }
	    if(debug)System.out.println("numBulletsEnemyShot = " + count);
        return count;
    }

    public static void shootOpposingBullets() throws GameActionException{
    	int bulletsToShoot = numBulletsEnemyShot();
    	if(bulletsToShoot < 7)
    		return;
    	Direction targetDir = here.directionTo(bulletTarget.location);
    	Direction leftTriadDir = targetDir.rotateLeftDegrees(20);
		Direction rightTriadDir = targetDir.rotateRightDegrees(20);
		Direction leftPentadDir = targetDir.rotateLeftDegrees(30);
		Direction rightPentadDir = targetDir.rotateRightDegrees(30);
		boolean ableToShootTriad = isDirSafe(leftTriadDir) && isDirSafe(rightTriadDir);
		boolean ableToShootPentad = isDirSafe(leftPentadDir) && isDirSafe(rightPentadDir);
		if(ableToShootPentad && bulletsToShoot > 10)
			rc.firePentadShot(targetDir);
		else if(ableToShootTriad)
			rc.fireTriadShot(targetDir);
    }

    private static float calcSafeDist(RobotInfo bestRobot) throws GameActionException {
		if(bestRobot.type == RobotType.SCOUT && rc.canSenseLocation(bestRobot.location) && rc.isLocationOccupiedByTree(bestRobot.location)){//edge case for scouts in trees
			return -1; //signal we are dealing with a scout
		}
		if(bestRobot.type == RobotType.GARDENER || bestRobot.type == RobotType.ARCHON || bestRobot.type == RobotType.SCOUT){
			return 0;
		}
		if(bestRobot.health < bestRobot.type.maxHealth){
			RobotInfo[] nearbyEnemiesToThatRobot = rc.senseNearbyRobots(bestRobot.location, 2, enemy);
			if(nearbyEnemiesToThatRobot.length > 1 && nearbyEnemiesToThatRobot[1].type == RobotType.GARDENER){
				return 0;
				//is being built
			}
		}
		//if(bestRobot.type == RobotType.TANK){
		//	return (float) (type.sensorRadius);
		//}
		if(type == RobotType.TANK){
			return (float)(type.sensorRadius + type.bodyRadius - 1);
		}
		if(bestRobot.type == RobotType.TANK){
			return (float)(type.sensorRadius + RobotType.TANK.bodyRadius - .1);
		}
		float safeDistance = /*dodgingOptionsLimited(here, bestRobot.location) +*/ bestRobot.type.bodyRadius + type.bodyRadius + bestRobot.type.strideRadius + 
				(bestRobot.type == RobotType.LUMBERJACK ? GameConstants.LUMBERJACK_STRIKE_RADIUS - bestRobot.type.bodyRadius : ((float)(bestRobot.type.bulletSpeed * (bestRobot.type == RobotType.SCOUT ? 1 : 1.8) - (nearbyAlliedRobots.length - 3 > nearbyEnemyRobots.length ? (nearbyAlliedRobots.length - nearbyEnemyRobots.length) / 10.0 : 0))));//for now ki
		/*if(safeDistance > type.sensorRadius - 0.05){
			safeDistance = (float)(type.sensorRadius - 0.05);
		}*/
		//if(debug)System.out.println("safeDist = " + safeDist);
		return safeDistance;
	}

	private static float dodgingOptionsLimited(MapLocation toHere, MapLocation targetLoc) throws GameActionException {
		Direction dir = here.directionTo(targetLoc);
		int count = 0;
		for(int i = 0; i < 4; i++){
			MapLocation check = toHere.add(dir, type.strideRadius);
			if(rc.isLocationOccupied(check) && !(rc.senseRobotAtLocation(here.add(dir, type.strideRadius)) != null && rc.senseRobotAtLocation(here.add(dir, type.strideRadius)).team == us)){
				count++;
			}
			dir = dir.rotateLeftDegrees(90);
		}
		if(debug)System.out.println("limited options = " + count);
		return (float) (count * 0.5);
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
		/*
		if(target.isBullet()){
		    return PENTAD_SHOT;
        }*/

		if(target == null || (rc.getTeamVictoryPoints() > 1000 - rc.getTreeCount() * 5 && rc.getTeamVictoryPoints() - rc.getOpponentVictoryPoints() < 50))
			return NO_SHOT;
		if(safeDist == -1){
			return SINGLE_SHOT;
		}
		RobotInfo targetRobot = null;
		MapLocation targetLoc = target.getLocation();
		Direction targetDir = here.directionTo(targetLoc);
		if(target.isRobot()){
			targetRobot = (RobotInfo)target;
			if(targetRobot.type == RobotType.ARCHON){
				if(targetRobot.health < 10 && (Message.ENEMY_ARCHONS_KILLED.getClosestLocation(targetRobot.location) == null || Message.ENEMY_ARCHONS_KILLED.getClosestLocation(targetRobot.location).distanceTo(targetRobot.location) > 2)){
					Message.ENEMY_ARCHONS_KILLED.addLocation(targetRobot.location);
				}
				return (nearbyAlliedRobots.length > 5 || (rc.getInitialArchonLocations(enemy).length - Message.ENEMY_ARCHONS_KILLED.getLength() < 2 && Message.GENETICS.getValue() != MapAnalysis.RUSH_ENEMY && rc.getTeamBullets() > 100) || rc.getTreeCount() > 10 || rc.getTeamBullets() > 500 ? SINGLE_SHOT: NO_SHOT);
			}
		}
		Direction leftTriadDir = targetDir.rotateLeftDegrees(20);
		Direction rightTriadDir = targetDir.rotateRightDegrees(20);
		Direction leftPentadDir = targetDir.rotateLeftDegrees(30);
		Direction rightPentadDir = targetDir.rotateRightDegrees(30);
		boolean ableToShootTriad = true;
		boolean ableToShootPentad = true;
		if(Clock.getBytecodesLeft() > 3000){
			ableToShootTriad = isDirSafe(leftTriadDir) && isDirSafe(rightTriadDir);
			ableToShootPentad = isDirSafe(leftPentadDir) && isDirSafe(rightPentadDir);
		}
		if(blockedByTree > 1 && nearbyNeutralTrees.length > 7){
			//in tight corridor aka line of fire, magic wood
			if(debug)System.out.println("in tight corridor");
			return SINGLE_SHOT;
		}
		int tempSV = singleValue;
		
		if(targetRobot != null && here.distanceTo(targetLoc) - type.bodyRadius - targetRobot.type.bodyRadius < type.bulletSpeed){
			if(willHitLoc(leftPentadDir, targetLoc, targetRobot.type.bodyRadius)){
				//if(debug)System.out.println("close enough to pentad");
				return PENTAD_SHOT;
			}
			if(willHitLoc(leftTriadDir, targetLoc, targetRobot.type.bodyRadius)){
				//if(debug)System.out.println("close enough to triad");
				return TRIAD_SHOT;
			}
		}
		
		for(RobotInfo a: nearbyAlliedRobots){
			if(a.type == RobotType.SOLDIER || a.type == RobotType.TANK){
				singleValue += 10;
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

		for(TreeInfo t: nearbyEnemyTrees){
			Direction dirToT = here.directionTo(t.location);
			float deg = Math.abs(targetDir.degreesBetween(dirToT));
			if(deg < PENTAD_SPREAD_DEGREES){
				//if(debug)System.out.println("added enemy tree to pentad");
				pentadValue += TREE_HIT_VALUE;
				if(deg < TRIAD_SPREAD_DEGREES){
					triadValue += TREE_HIT_VALUE;
				}
			}
		}
		
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
		int treeMod = rc.getTreeCount() / 4;
		int victoryPointMod = (rc.getOpponentVictoryPoints() - rc.getTeamVictoryPoints() > - 50 ? (rc.getTeamVictoryPoints() - 700) / 20 : 0);
		float opponentBulletsMod = (float) (numBulletsEnemyShot() * 4.5);
		if(debug)System.out.println("opponentBulletsMod = " + opponentBulletsMod);
		if(victoryPointMod < 0)
			victoryPointMod = 0;
		if (ableToShootPentad && pentadValue + treeMod - victoryPointMod + opponentBulletsMod + (type.attackPower + type.bulletSpeed) * 4 > 121 ) {
			if(debug)System.out.println("Pentad");
			return PENTAD_SHOT;
		}
		if (ableToShootTriad && triadValue + treeMod - victoryPointMod + opponentBulletsMod + (type.attackPower + type.bulletSpeed) * 4 > 106) {
			if(debug)System.out.println("Triad");
			return TRIAD_SHOT;
		}
		if(tempSV > 69 || targetRobot.type == RobotType.TANK){
			if(debug)System.out.println("Single");
			return SINGLE_SHOT;
		}
		//System.out.println(here.distanceTo(targetLoc));
		if(type == RobotType.SOLDIER && here.distanceTo(targetLoc) < safeDist + (nearbyAlliedRobots.length - 3 > nearbyEnemyRobots.length ? (nearbyAlliedRobots.length - nearbyEnemyRobots.length) / 10.0 : 0)){
			if(victoryPointMod < 10){
				if(debug)System.out.println("Triad");
				return TRIAD_SHOT;
			}
		}
		if(targetRobot.type == RobotType.GARDENER){
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
		int limit = 15;
		float dist = here.distanceTo(target.location);
		//if(debug)System.out.println("starting isDirSafe " + Clock.getBytecodeNum());
		Direction intendedAttackDir = here.directionTo(target.location);
		for (RobotInfo friend : nearbyAlliedRobots) {
			if (friend.location.distanceTo(here) < dist - type.bodyRadius
					- target.type.bodyRadius){ 
				if (willHitLoc(intendedAttackDir, friend.location, friend.type.bodyRadius)) {
					//if(debug)System.out.println("Direction is not safe");
					return false;
				}
			} else {
				break;
			}

		}
		for (RobotInfo enemy: nearbyEnemyRobots) {
			if(enemy.getID() == target.getID())
				continue;
			if (enemy.location.distanceTo(here) < dist - type.bodyRadius
					- target.type.bodyRadius){ 
				if (willHitLoc(intendedAttackDir, enemy.location, enemy.type.bodyRadius)) {
					//if(debug)System.out.println("Direction is not safe");
					return false;
				}
			} else {
				break;
			}

		}
		int count = 0;
		for (TreeInfo friend : nearbyTrees) {
			count++;
			if(count > limit){
				break;
			}
			if(friend.team == enemy && nearbyAlliedRobots.length > 0){
				continue;
			}
			if(here.distanceTo(friend.location) - friend.radius - type.bodyRadius < 0.1 && dist - target.type.bodyRadius - type.bodyRadius < 0.1)
				continue;
			if (friend.location.distanceTo(here) - friend.radius - target.type.bodyRadius < dist) {
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
//			if (friend.location.distanceTo(here) < dist - type.bodyRadius
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
	
	//like above but takes location rather than ri
	public static boolean isDirSafe(Direction dir) throws GameActionException {
		//if(debug)System.out.println("starting isDirSafe " + Clock.getBytecodeNum());
		Direction intendedAttackDir = dir;
		int limit = 10;
		int count = 0;
		for (TreeInfo friend : rc.senseNearbyTrees(here.add(dir, type.sensorRadius), -1, Team.NEUTRAL)) {
			count++;
			if(count > limit)
				break;
			if(friend.team == enemy){
				continue;
			}
			if (willHitLoc(intendedAttackDir, friend.location, friend.radius)) {
				System.out.println(blockedByTree);
				blockedByTree++;
				//if(debug)System.out.println("Direction is not safe");
				return false;
			}
		}
		limit = 5;
		count = 0;
		for (RobotInfo friend : nearbyAlliedRobots) {
			count++;
			if(count > limit)
				break;
			if (willHitLoc(intendedAttackDir, friend.location, friend.type.bodyRadius)) {
				//if(debug)System.out.println("Direction is not safe");
				return false;
			}
		}
		//if(debug)System.out.println("end dir safe " + Clock.getBytecodeNum());
//		for (TreeInfo friend : nearbyNeutralTrees) {
//			if (friend.location.distanceTo(here) < dist - type.bodyRadius
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


	public static boolean willHitLoc(Direction dir, MapLocation loc, float rad) throws GameActionException{
		Direction directionToHere = here.directionTo(loc);
		float theta = Math.abs(dir.radiansBetween(directionToHere));
		if (theta > Math.PI / 2) {
			return false;
		}
		float distToTree = here.distanceTo(loc);
		float perpendicularDist = (float) (distToTree * Math.sin(theta));
		return perpendicularDist < rad + .01;
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
	
	/* stuff not in use below
	 * 
	 * 	public static class BulletAura{
		MapLocation edgeMidA;
		MapLocation edgeMidB;
		MapLocation center;
		float radius;
		float speed;
		Direction dir;
		float xOffset;
		float yOffset;
		float ang;
		float damage;
		
		public BulletAura(MapLocation edMA, MapLocation edMB, float rad, float s, Direction d, float dmg){
			this.edgeMidA = (edMA.x < edMB.x ? edMA : edMB);
			this.edgeMidB = (edMA.x >= edMB.x ? edMA : edMB);
			this.center = Util.midpoint(edMA, edMB);
			this.radius = rad;
			this.speed = s;
			this.dir = d;
			this.setUpRotation();
			this.damage = dmg;
		}
		
		public float getDamage(){
			return this.damage;
		}
		
		public boolean bulletWillHitLoc(MapLocation loc){
			if(loc.distanceTo(edgeMidA) < radius || loc.distanceTo(edgeMidB) < radius)
				return true;
			float x = (float) ((loc.x - xOffset) * Math.cos(ang) - (loc.y - yOffset) * Math.sin(ang));
			float y = (float) ((loc.y - yOffset) * Math.cos(ang) + (loc.x - xOffset) * Math.sin(ang));
			return Math.abs(x) < speed/2 && Math.abs(y) < radius;
		}
		
		public void incrementTurn(){
			this.edgeMidA = this.edgeMidA.add(dir, speed);
			this.edgeMidB = this.edgeMidB.add(dir, speed);
			this.center = Util.midpoint(edgeMidA, edgeMidB);
		}
		
		private void setUpRotation(){
			this.ang = center.directionTo(edgeMidB).radians;
			this.xOffset = center.x;
			this.yOffset = center.y;	
		}
		
	}
	
	private static float[] incrementScores(BulletAura[] bs, int bsSize, MapLocation moveTo, float[] scores, int i, float bestScore){
		for(int j = 0; j < bsSize; j++){
			int start = Clock.getBytecodeNum();
			boolean willHit = bs[j].bulletWillHitLoc(moveTo);
			System.out.println("will collide alternative takes " + (Clock.getBytecodeNum() - start));
			if(willHit){
				scores[i] += bs[j].getDamage();
				if(scores[i] > bestScore)
					break;
			}
		}
		return scores;
	}

	private static MapLocation lineCircleIntersectionOne(MapLocation startLoc, MapLocation endLoc, MapLocation center, float rad){
		float accuracy = (float)(.2);
		MapLocation midLoc = Util.midpoint(startLoc, endLoc);
		while(center.distanceTo(midLoc) - rad > accuracy){
			if(Math.abs(center.distanceTo(startLoc) - rad) < Math.abs(center.distanceTo(startLoc) - rad)){
				endLoc = midLoc;
			}
			else{
				startLoc = midLoc;
			}	
			midLoc = Util.midpoint(startLoc, endLoc);
		}
		return midLoc;
	}
	
	private static MapLocation[] lineCircleIntersectionTwo(MapLocation startLoc, MapLocation endLoc, MapLocation center, float rad){
		MapLocation[] ret = new MapLocation[2];
		return ret;
	}
		
	private static MapLocation[] circleCircleIntersectionTwo(MapLocation center1, MapLocation center2, float rad1, float rad2){
		//center 1 is bigger circle aka bullet circle
		MapLocation[] ret = new MapLocation[2];
		float d = center1.distanceTo(center2);
		float r = rad2;
		float R = rad1;
		float distA = (d * d - r * r + R * R) / (2 * d);
		float distB = (float)(Math.sqrt((-d + r - R) * (-d - r + R) * (-d + r + R) * (d + r + R)) / (2 * d));
		Direction cDir = center1.directionTo(center2);
		ret[0] = center1.add(cDir,distA).add(cDir.rotateLeftDegrees(90), distB);
		ret[1] = center1.add(cDir,distA).add(cDir.rotateLeftDegrees(90), distB);
		return ret;
	}
	
	private static float[] increaseScoresOnePly(BulletInfo b, float[] scores, Direction[] options, MapLocation toHere, float radius) throws GameActionException {
		MapLocation bLoc = b.location;
		//if(debug)rc.setIndicatorLine(toHere, bLoc, 0, 255, 0);
		Direction bDir = b.dir;
		MapLocation onePlyLoc = bLoc.add(bDir, b.speed);
		float bLocDist = bLoc.distanceTo(here);
		float onePlyLocDist = bLoc.distanceTo(here);
		MapLocation midLoc = Util.midpoint(bLoc, onePlyLoc);
		MapLocation cornerA = bLoc.add(b.dir.rotateLeftDegrees(90), type.bodyRadius);
		MapLocation cornerB = bLoc.add(b.dir.rotateRightDegrees(90), type.bodyRadius);
		MapLocation cornerC = onePlyLoc.add(b.dir.rotateRightDegrees(90), type.bodyRadius);
		MapLocation cornerD = onePlyLoc.add(b.dir.rotateLeftDegrees(90), type.bodyRadius);
		MapLocation[] locsOfInterest = new MapLocation[2];
		if(bLocDist < type.bodyRadius - radius || onePlyLocDist < type.bodyRadius - radius){
			//case 1: we're fucked
			return scores;
		}
		else if(cornerA.distanceTo(toHere) < radius || cornerB.distanceTo(toHere) < radius){
			//case 4: semi and line
			//semi
			MapLocation circCenter = bLoc.distanceTo(toHere) < onePlyLoc.distanceTo(toHere) ? bLoc: onePlyLoc;
			MapLocation[] semiLocs = circleCircleIntersectionTwo(circCenter, toHere, type.bodyRadius, radius); 
			locsOfInterest[0] = semiLocs[0].distanceTo(midLoc) > semiLocs[1].distanceTo(midLoc) ? semiLocs[0]: semiLocs[1];
			//line
			float slope = (cornerA.y - cornerB.y) / (cornerA.x - cornerB.x);
			float intercept = cornerA.y - slope * cornerA.x;
			MapLocation[] lineLocs = intersectionsBetweenLineAndCircle(slope, intercept, toHere, radius); 
			locsOfInterest[1] = lineLocs[0].distanceTo(midLoc) < lineLocs[1].distanceTo(midLoc) ? lineLocs[0]: lineLocs[1];
		}
		else if(cornerC.distanceTo(toHere) < radius || cornerD.distanceTo(toHere) < radius){
			//case 4: semi and line
			//semi
			MapLocation circCenter = bLoc.distanceTo(toHere) < onePlyLoc.distanceTo(toHere) ? bLoc: onePlyLoc;
			MapLocation[] semiLocs = circleCircleIntersectionTwo(circCenter, toHere, type.bodyRadius, radius); 
			locsOfInterest[0] = semiLocs[0].distanceTo(midLoc) > semiLocs[1].distanceTo(midLoc) ? semiLocs[0]: semiLocs[1];
			//line
			float slope = (cornerC.y - cornerD.y) / (cornerC.x - cornerD.x);
			float intercept = cornerC.y - slope * cornerD.x;
			MapLocation[] lineLocs = intersectionsBetweenLineAndCircle(slope, intercept, toHere, radius); 
			locsOfInterest[1] = lineLocs[0].distanceTo(midLoc) < lineLocs[1].distanceTo(midLoc) ? lineLocs[0]: lineLocs[1];
		}
		else if(bLoc.distanceTo(toHere) < radius || onePlyLoc.distanceTo(toHere) < radius){
			//case 3: two semis
			MapLocation circCenter = bLoc.distanceTo(toHere) < onePlyLoc.distanceTo(toHere) ? bLoc: onePlyLoc;
			locsOfInterest = circleCircleIntersectionTwo(circCenter, toHere, type.bodyRadius, radius); 
		}
		else{
			MapLocation[] lineLocs;
			if(cornerA.distanceTo(toHere) < cornerD.distanceTo(toHere)){
				float slope = (cornerA.y - cornerB.y) / (cornerA.x - cornerB.x);
				float intercept = cornerA.y - slope * cornerA.x;
				lineLocs = intersectionsBetweenLineAndCircle(slope, intercept, toHere, radius); 
			}
			else{
				float slope = (cornerC.y - cornerD.y) / (cornerC.x - cornerD.x);
				float intercept = cornerC.y - slope * cornerD.x;
				lineLocs = intersectionsBetweenLineAndCircle(slope, intercept, toHere, radius); 
			}
			if(lineLocs[0] != null){
				//case 2: 2 lines
				locsOfInterest = lineLocs;
			}
			//case 1 or case 5: either we're fucked either way or we're home free
		}
		if(locsOfInterest[0] != null){
			int[] startEndVals = determineStartAndEndVals(locsOfInterest, scores.length, toHere);
			if(startEndVals[0] < startEndVals[1]){
				for(int i = startEndVals[0]; i <= startEndVals[1]; i++){
					scores[i] += b.damage;
				}
			}
			else{
				for(int i = startEndVals[0]; i < scores.length; i++){
					scores[i] += b.damage;
				}
				for(int i = 1; i <= startEndVals[1]; i++){
					scores[i] += b.damage;
				}
			System.out.println("hi");
			}
			if(willCollide(b, toHere)){
				scores[0] += b.damage;
			}
		}
		int[] added = new int[scores.length];
		boolean enteredCircle = false;
		int divideBy = 360 / (scores.length - 1);
		float dist = here.distanceTo(bLoc);
		for(int i = 0; i < precision; i++){
			dist = toHere.distanceTo(bLoc);
			if(added[0] == 0 && dist < type.bodyRadius){
				scores[0] += b.damage;
				added[0] = 1;
			}
			if(dist < radius){
				enteredCircle = true;
				float angOnEachSide = (float)(Math.abs(Math.asin(type.bodyRadius/dist) * 180 / Math.PI));
				System.out.println("ang on each side = " + angOnEachSide);
				int mid = (int)(myDegreesBetween(refDir, toHere.directionTo(bLoc)) / divideBy);
				if(debug)rc.setIndicatorLine(toHere, toHere.add(options[mid], type.strideRadius), 255, 0, 0);
				int addsub = (int)(angOnEachSide) / divideBy;//+ 1 for now to be extra careful
				for(int j = mid - addsub; j < mid + addsub; j++){
					if(j < 0 || j >= scores.length){
						continue;
					}
					if(added[j] == 0){
						scores[j] += b.damage;
						//if(debug)rc.setIndicatorLine(toHere, toHere.add(options[j], type.strideRadius), 255, 0, 0);
					}
				}
			}
			else if(enteredCircle){
				break;
			}
			if(dist < type.bodyRadius - type.strideRadius + 0.05){
				//we're fucked
				rip = true;
				for(int j = 0; j < scores.length; j++){
					if(added[j] == 0){
						scores[j] += b.damage;
						if(debug)rc.setIndicatorLine(toHere, toHere.add(options[j], type.strideRadius), 255, 0, 0);
					}
				}
				break;
			}
			if(dist < radius){
				enteredCircle = true;
				int toAdd = (int)(myDegreesBetween(refDir, toHere.directionTo(bLoc)) / divideBy);
				if(added[toAdd] == 0){
					scores[toAdd] += b.damage;
					if(debug)rc.setIndicatorLine(toHere, toHere.add(options[toAdd], type.strideRadius), 255, 0, 0);
				}
				if(added[toAdd + 1] == 0){
					scores[toAdd + 1] += b.damage;
					if(debug)rc.setIndicatorLine(toHere, toHere.add(options[toAdd + 1], type.strideRadius), 255, 0, 0);
				}
			}	
			bLoc = bLoc.add(b.dir, b.speed/precision); 
			}
		return scores;
	}

	private static int[] increaseScoresLineThroughCircle(float bLineSlope, float bLineIntercept, int[] scores, MapLocation targetLoc, MapLocation bLoc, MapLocation onePlyLoc, MapLocation toHere, float distToMove, float dmg, Direction[] options) throws GameActionException {
		//if(debug)System.out.println("start of incrementing scores = " + Clock.getBytecodeNum());
		MapLocation[] outerCircleLocs = intersectionsBetweenLineAndCircle(bLineSlope, bLineIntercept, toHere, distToMove + type.bodyRadius);
		if(outerCircleLocs[0] == null){
			//case 1: line does not go through circle aka bullet started and ended outside of circle and didn't pass through it
			if(debug)System.out.println("case 1");
			return scores;
		}
		boolean bLocInOuterCirc = toHere.distanceTo(bLoc) < type.bodyRadius + distToMove;
		boolean onePlyLocInOuterCirc = toHere.distanceTo(onePlyLoc) < type.bodyRadius + distToMove;
		if(!(bLocInOuterCirc || onePlyLocInOuterCirc)){
			//case 2: bullet passed through circle without starting inside
			if(debug)System.out.println("case 2");		
		}
		else if(bLocInOuterCirc && onePlyLocInOuterCirc){
			//case 3: bullet started and ended inside circle
			MapLocation start = bLoc;
			MapLocation end = onePlyLoc;
			MapLocation sExtendedA = start.add(start.directionTo(toHere).rotateLeftDegrees(90), type.bodyRadius);
			MapLocation sExtendedB = start.add(start.directionTo(toHere).rotateRightDegrees(90), type.bodyRadius);
			MapLocation eExtendedA = end.add(end.directionTo(toHere).rotateLeftDegrees(90), type.bodyRadius);
			MapLocation eExtendedB = end.add(end.directionTo(toHere).rotateRightDegrees(90), type.bodyRadius);
			outerCircleLocs[0] = sExtendedA.distanceTo(end) > sExtendedB.distanceTo(end) ? sExtendedA: sExtendedB;
			outerCircleLocs[1] = eExtendedA.distanceTo(start) > eExtendedB.distanceTo(start) ? eExtendedA: eExtendedB;
			if(debug)System.out.println("case 3");
		}
		else if(bLocInOuterCirc){
			//case 4: bullet started inside circle and ended outside of it
			MapLocation start = bLoc;
			MapLocation end = onePlyLoc;
			MapLocation sExtendedA = start.add(start.directionTo(toHere).rotateLeftDegrees(90), type.bodyRadius);
			MapLocation sExtendedB = start.add(start.directionTo(toHere).rotateRightDegrees(90), type.bodyRadius);
			float degreesBtwnExtensions = Math.abs(toHere.directionTo(sExtendedA).degreesBetween(toHere.directionTo(sExtendedB)));
			boolean addA = sExtendedA.distanceTo(end) > sExtendedB.distanceTo(end);
			MapLocation addToArray = addA ? sExtendedA: sExtendedB;
			int index = (Math.abs(outerCircleLocs[0].distanceTo(end) - outerCircleLocs[0].distanceTo(start)
					- end.distanceTo(start) < .01 ? 0 : 1));
			float degreesBtwnExtensionAndIntersect = Math.abs(toHere.directionTo(addToArray).degreesBetween(toHere.directionTo(outerCircleLocs[index == 0 ?1:0])));
			if(degreesBtwnExtensions < degreesBtwnExtensionAndIntersect){
				outerCircleLocs[index] = addToArray;
			}
			else{
				outerCircleLocs[0] = sExtendedA;
				outerCircleLocs[1] = sExtendedB;
			}
			if(debug)System.out.println("case 4");
		}
		else{
			//case 5: bullet started outside circle and ended inside
			MapLocation start = bLoc;
			MapLocation end = onePlyLoc;
			MapLocation eExtendedA = end.add(end.directionTo(toHere).rotateLeftDegrees(90), type.bodyRadius);
			MapLocation eExtendedB = end.add(end.directionTo(toHere).rotateRightDegrees(90), type.bodyRadius);
			float degreesBtwnExtensions = Math.abs(toHere.directionTo(eExtendedA).degreesBetween(toHere.directionTo(eExtendedB)));
			//boolean addA = eExtendedA.distanceTo(start) > eExtendedB.distanceTo(start);
			MapLocation addToArray = eExtendedA.distanceTo(start) < eExtendedB.distanceTo(start) ? eExtendedA: eExtendedB;
			int index = (Math.abs(outerCircleLocs[0].distanceTo(start)
                    - outerCircleLocs[0].distanceTo(end)
					- end.distanceTo(start)) < .01 ? 0 : 1);
			float degreesBtwnExtensionAndIntersect = Math.abs(toHere.directionTo(addToArray).degreesBetween(toHere.directionTo(outerCircleLocs[index == 0 ?1:0])));
			if(degreesBtwnExtensions < degreesBtwnExtensionAndIntersect){
				outerCircleLocs[index] = addToArray;
			}
			else{
				outerCircleLocs[0] = eExtendedA;
				outerCircleLocs[1] = eExtendedB;
			}
			if(debug)System.out.println("case 5");
		}
		if(debug)rc.setIndicatorLine(toHere, bLoc, 0, 0, 255);
		if(debug)System.out.println("locs of interest are " + outerCircleLocs[0] + " " + outerCircleLocs[1]);
		int[] startEndVals = determineStartAndEndVals(outerCircleLocs, scores.length, toHere);
		//MapLocation[] innerCircleLocs = intersectionsBetweenLineAndCircle(bLineSlope, bLineIntercept, bLoc, onePlyLoc, toHere, distToMove);
		//int lowSafe = scores.length;
		//int highSafe = 0;
		if(innerCircleLocs[0] != null){
			int[] innerStartEndVals = determineStartAndEndVals(innerCircleLocs, scores.length, toHere, targetLoc);
			lowSafe = innerStartEndVals[0];
			highSafe = innerStartEndVals[0];
		}
		if(debug)System.out.println("before for loop = " + Clock.getBytecodeNum());
		if(startEndVals[0] < startEndVals[1]){
			for(int i = startEndVals[0]; i <= startEndVals[1]; i++){
			if(debug)rc.setIndicatorLine(toHere, toHere.add(options[i], type.strideRadius), 255, 0, 0);
			scores[i] += dmg;
			}
		}
		else{
			for(int i = startEndVals[0]; i < scores.length; i++){
				if(debug)rc.setIndicatorLine(toHere, toHere.add(options[i], type.strideRadius), 255, 0, 0);
				scores[i] += dmg;
			}
			for(int i = 1; i <= startEndVals[1]; i++){
				if(debug)rc.setIndicatorLine(toHere, toHere.add(options[i], type.strideRadius), 255, 0, 0);
				scores[i] += dmg;
			}
		}
		if(debug)System.out.println("end of incrementing scores = " + Clock.getBytecodeNum());
		return scores;
	}

	private static int[] determineStartAndEndVals(MapLocation[] locsOfInterest, int length, MapLocation toHere) {
		//int startB = Clock.getBytecodeNum();
		double[] degs = {toHere.directionTo(locsOfInterest[0]).radians * 180 / Math.PI, toHere.directionTo(locsOfInterest[1]).radians * 180 / Math.PI};
		for(int i = 0; i < degs.length; i++){
			if(degs[i] < 0){
				degs[i] = 360 + degs[i];
			}
		}
		if(degs[1] < degs[0] || degs[0] - degs[1] > 180){
			double temp = degs[1];
			degs[1] = degs[0];
			degs[0] = temp;
		}
		int start = (int)(degs[0]) / (360 / (length - 1)) + 1;
		int end = (int)(degs[1]) / (360 / (length - 1));
		//System.out.println("DSAEV took " + (Clock.getBytecodeNum() - startB));
		return new int[]{start,end};
	}

	private static boolean lineAndCircleIntersect(float bLineSlope, float bLineIntercept, MapLocation toHere, float radius){
		float a = 1 + bLineSlope * bLineSlope;
		float b = 2 * (bLineSlope * bLineIntercept - toHere.x - bLineSlope * toHere.y);
		float c = toHere.x * toHere.x + bLineIntercept * bLineIntercept + toHere.y * toHere.y - 2 * bLineIntercept * toHere.y - radius * radius;
		float det = b * b - 4 * a * c;
		if(det < 0){
			return false;
		}
		return true;
	}
	private static MapLocation[] intersectionsBetweenLineAndCircle(float bLineSlope, float bLineIntercept, MapLocation toHere, float radius){
		//quadratic formula
		//int startB = Clock.getBytecodeNum();
		//System.out.println("bLineSlope = " + bLineSlope + " bLineIntercept = " + bLineIntercept + "radius = " + radius);
		//System.out.println("QUAD " + (Clock.getBytecodeNum()));
		MapLocation[] ret = {null, null};
		float a = 1 + bLineSlope * bLineSlope;
		float b = 2 * (bLineSlope * bLineIntercept - toHere.x - bLineSlope * toHere.y);
		float c = toHere.x * toHere.x + bLineIntercept * bLineIntercept + toHere.y * toHere.y - 2 * bLineIntercept * toHere.y - radius * radius;
		float det = b * b - 4 * a * c;
		if(det < 0){
			return ret;
		}
		float x1 = (float)((-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a));
		float y1 = bLineSlope * x1 + bLineIntercept;
		float x2 = (float)((-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a));
		float y2 = bLineSlope * x2 + bLineIntercept;
		MapLocation[] retMe = {new MapLocation(x1, y1), new MapLocation(x2, y2)};
		//System.out.println("IBLAC took " + (Clock.getBytecodeNum() - startB));
		return retMe;
	}
	
	private static double myDegreesBetween(Direction direction, Direction direction2) {
		double theirDegreesBetween = direction.degreesBetween(direction2);
		return (theirDegreesBetween > 0 ? theirDegreesBetween : 360 + theirDegreesBetween);
	}*/
}