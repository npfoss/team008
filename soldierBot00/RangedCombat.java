package team008.soldierBot00;

import battlecode.common.*;

public class RangedCombat extends Bot {

	private static final String SINGLE_SHOT = "single shot";
	private static final String TRIAD_SHOT = "triad shot";
	private static final String PENTAD_SHOT = "pentad shot";
	private static final String NO_SHOT = "no shot";
	private static final int WORTHWHILE_SHOT_THRESHOLD = 70;
	private static float safeDist;
	private static float moveDist;
	private static boolean bulletSafe;

	/*
	 * to call execute, number of enemies must be > 0
	 */
	public static void execute() throws GameActionException {

		//int temp = Clock.getBytecodeNum();
		//System.out.println("moving used: " + (Clock.getBytecodeNum() - temp));
		safeDist = 0;
		potentialAttackStats attack = chooseTargetAndShotType();
		BodyInfo target = attack.getTarget();
		if(target == null){
			//System.out.println("rip target");
			tryMoveDirection(here.directionTo(MapAnalysis.center), true, false);
			return;
		}
		MapLocation targetLoc = target.getLocation();
		//Direction targetDir = here.directionTo(attack.getTarget().getLocation());
		//Direction moveDir = (targetDir);
		moveDist = type.strideRadius;
		Direction moveDir = calcMoveDir(targetLoc);
		if (moveDir != null) {
			MapLocation nextLoc = here.add(moveDir, type.strideRadius);;
			if (nextLoc.distanceTo(targetLoc) < here.distanceTo(targetLoc)) {
				//if(debug){System.out.println("moved before shooting");}
				rc.move(moveDir, moveDist);
			} 
		}
		parseShotTypeAndShoot(attack.getTarget(), attack.getShotType());
		if(rc.getMoveCount()  == 0 && moveDir != null){
			//System.out.println("shot before moving");
			rc.move(moveDir, moveDist);
		}
	}

	private static Direction calcMoveDir(MapLocation targetLoc) {
		Direction targetDir = here.directionTo(targetLoc);
		float maxDist = -99999;
		Direction backupDir = null;
		if(Math.abs(safeDist -1) < .01){//dealing with scout in tree
			System.out.println("hi");
			MapLocation targetSpot = targetLoc.add(targetDir.opposite(), (float) 2.004);
			float dist = here.distanceTo(targetSpot);
			if(dist < type.strideRadius && rc.canMove(targetDir, dist)){
				moveDist = dist;
				return targetDir;
			}
		}
		Direction dir = here.directionTo(targetLoc);
		if(rc.canMove(dir, type.strideRadius)){
			MapLocation moveTo = here.add(dir, type.strideRadius);
			if(isSafe(moveTo, targetLoc))
				return dir;
			else if (bulletSafe && moveTo.distanceTo(targetLoc) > maxDist){
				maxDist = moveTo.distanceTo(targetLoc);
				backupDir = dir;
			}
		}
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for(int i = 0; i < 18; i++){
			if(rc.canMove(left, type.strideRadius)){
				MapLocation moveTo = here.add(left, type.strideRadius);
				if(isSafe(moveTo, targetLoc))
					return left;
				else if (bulletSafe && moveTo.distanceTo(targetLoc) > maxDist){
					maxDist = moveTo.distanceTo(targetLoc);
					backupDir = left;
				}
			}
			if(rc.canMove(right, type.strideRadius)){
				MapLocation moveTo = here.add(right, type.strideRadius);
				if(isSafe(moveTo, targetLoc))
					return right;
				else if (bulletSafe && moveTo.distanceTo(targetLoc) > maxDist){
					maxDist = moveTo.distanceTo(targetLoc);
					backupDir = right;
				}
			}
			left = left.rotateLeftDegrees(10);
			right = right.rotateRightDegrees(10);
		}
		return backupDir;
	}

	private static boolean isSafe(MapLocation loc, MapLocation targetLoc) {
		//System.out.println("safe dist = " + safeDist);
		bulletSafe = true;
		for (BulletInfo b : nearbyBullets) {
			if (willCollide(b, loc)) {
				bulletSafe = false;
				return false;
			}
		}
		if(Math.abs(safeDist) < .01){
			if(nearbyEnemyRobots.length == 0 || nearbyEnemyRobots.length == 1 && nearbyEnemyRobots[0].type == RobotType.GARDENER)
				return true;
			else{
				RobotInfo[] nearbyEs = rc.senseNearbyRobots(loc, -1, enemy); //robots closest to gardener or tree
				for(RobotInfo e: nearbyEs){
					if(e.type != RobotType.GARDENER){
						loc = e.location;
						safeDist = e.type.bodyRadius + type.bodyRadius + e.type.strideRadius + (e.type == RobotType.LUMBERJACK ? GameConstants.LUMBERJACK_STRIKE_RADIUS : e.type.bulletSpeed);
					}				
				}
			}
		}
		if(loc.distanceTo(targetLoc) < safeDist)
			return false;
		return true;
	}

	/**
	 * Tries to asses if its work shooting.
	 *
	 * @return
	 */
	/*
	private static boolean worthShooting(int shotValue) {
		return shotValue > WORTHWHILE_SHOT_THRESHOLD;
	}*/

	///////////////////// Shooting and Target Micro/////////////////////

	/**
	 * Picks the target
	 *
	 * @return
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
			canWeHitThemValue = canWeHitHeuristic(robot);
			score = (int) (canWeHitThemValue);

			if (score > bestScore && isDirectionSafe(robot)) {
				attackingGardener = (robot.type == RobotType.GARDENER);
				bestScore = score;
				bestRobot = robot;
				shotValue = canWeHitThemValue;
				if(attackingGardener){
					safeDist = 0;
					break;
				}
			}
			calculated++;
			if (calculated == robotsToCalculate){
				break;
			}
		}
		if(bestRobot != null && !attackingGardener){
			if(bestRobot.type == RobotType.SCOUT && rc.isLocationOccupiedByTree(bestRobot.location)){//edge case for scouts in trees
				safeDist = -1; //signal we are dealing with a scout
			}	
			else{
				safeDist = bestRobot.type.bodyRadius + type.bodyRadius + bestRobot.type.strideRadius + (bestRobot.type == RobotType.LUMBERJACK ? GameConstants.LUMBERJACK_STRIKE_RADIUS - bestRobot.type.bodyRadius : bestRobot.type.bulletSpeed);
			}//System.out.println("Safe dist = " + safeDist);
		}
		return new potentialAttackStats(bestRobot, calculateShotType(bestRobot, shotValue), shotValue);
	}

	/**
	 * An attempt to take shots we can make
	 *
	 * @param robot
	 * @return
	 */
	public static int canWeHitHeuristic(RobotInfo robot) {
		int score = 70;
		float howFarAwayTheyCanGet = here.distanceTo(robot.location) - type.bulletSpeed - type.bodyRadius
				- robot.type.bodyRadius + robot.type.strideRadius;
		//System.out.println("how far away they can get = " + howFarAwayTheyCanGet + " for robot at " + robot.location);
		//score -= 25 * howFarAwayTheyCanGet / (nearbyTrees.length + 1);
		score -= 20 * (howFarAwayTheyCanGet > 0 ? howFarAwayTheyCanGet: 0);
		score += (robot.type == RobotType.GARDENER ? 20 : 0);
		return score;
	}

	/**
	 * Picks whether to shot 1,3 or 5 bullets.
	 *
	 * @return the shot type
	 * @throws GameActionException
	 */
	public static String calculateShotType(BodyInfo target, int shotValue) throws GameActionException {
		// come up with some sort of formula for choosing the kind of shot
		int pentadValue = shotValue;
		int triadValue = shotValue;
		if(target == null)
			return NO_SHOT;
		Direction targetDir = here.directionTo(target.getLocation());
		for(TreeInfo t: nearbyEnemyTrees){
			Direction dirToT = here.directionTo(t.location);
			float deg = targetDir.degreesBetween(dirToT);
			if(deg < 30){
				pentadValue += 5;
				if(deg < 20){
					triadValue += 5;
				}
			}
		}
		for(RobotInfo r: nearbyEnemyRobots){
			float howFarAwayTheyCanGet = here.distanceTo(r.location) - type.bulletSpeed - type.bodyRadius
					- r.type.bodyRadius + r.type.strideRadius;
			float valFromHitting = 7 - howFarAwayTheyCanGet * 2;
			if(valFromHitting < 0)
				break;
			Direction dirToR = here.directionTo(r.location);
			float deg = targetDir.degreesBetween(dirToR);
			if(deg < 30){
				pentadValue += valFromHitting;
				if(deg < 20){
					triadValue += valFromHitting;
				}
			}
		}
		float score = shotValue;
		System.out.println("Pentad Value: " + pentadValue);
		System.out.println("Triad Value: " + triadValue);
		/*if (target != null) {
			if (here.distanceTo(target.getLocation()) - type.bulletSpeed - type.bodyRadius - target.getRadius() < 0) {
				score = 7;
			}
		}*/
		int trees = rc.getTreeCount();
		if (pentadValue > 110) {
			return PENTAD_SHOT;
		}
		if (score > 95) {
			return TRIAD_SHOT;
		}
		if (score > 69){
			return SINGLE_SHOT;
		}
		return NO_SHOT;
		
	}

	/**
	 * Determines if it is worth shooting, checks for friendly fire.
	 *
	 * @param target
	 * @param shotType
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
		Direction intendedAttackDir = here.directionTo(target.location);
		for (RobotInfo friend : nearbyAlliedRobots) {
			if (friend.location.distanceTo(here) < here.distanceTo(target.location) - type.bodyRadius
					- target.type.bodyRadius) {

				if (intendedAttackDir.radiansBetween(here.directionTo(friend.location)) < Math.PI / 12) {
					return false;
				}
			} else {
				break;
			}

		}
		for (TreeInfo friend : nearbyAlliedTrees) {
			if (friend.location.distanceTo(here) < here.distanceTo(target.location) - type.bodyRadius
					- target.type.bodyRadius) {
				if (intendedAttackDir.radiansBetween(here.directionTo(friend.location)) < Math.PI / 12) {
					return false;
				}
			} else {
				break;
			}
		}
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

	/**
	 * finds the number of allies that can also see this loc.
	 *
	 * @param loc
	 *            place to check if they can see
	 * @return number of allies who can see the loc
	 */
	public static int numOtherAlliesInSightRange(MapLocation loc) {
		int ret = 0;
		for (RobotInfo ally : nearbyAlliedRobots) {
			if (ally.getType().sensorRadius > loc.distanceTo(ally.location)
					&& (ally.type != RobotType.ARCHON || ally.type != RobotType.GARDENER))
				ret++;
		}
		return ret;
	}

	///////////////////// These Might Belong in Util/////////////////////
	public static void shootSingleShot(BodyInfo target) throws GameActionException {
		if (rc.canFireSingleShot() && target != null) {
			rc.fireSingleShot(rc.getLocation().directionTo(target.getLocation()));
		}

	}

	public static void shootTriadShot(BodyInfo target) throws GameActionException {
		if (rc.canFireTriadShot() && target != null) {
			rc.fireTriadShot(rc.getLocation().directionTo(target.getLocation()));
		}

	}

	public static void shootPentadShot(BodyInfo target) throws GameActionException {
		if (rc.canFirePentadShot() && target != null) {
			rc.firePentadShot(rc.getLocation().directionTo(target.getLocation()));
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