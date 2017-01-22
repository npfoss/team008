package team008.finalBot;

import battlecode.common.*;


public class RangedCombat extends Bot {

	//Finals
	private static final String SINGLE_SHOT = "single shot";
	private static final String TRIAD_SHOT = "triad shot";
	private static final String PENTAD_SHOT = "pentad shot";
	private static final String NO_SHOT = "no shot";
	private static final int TREE_HIT_VALUE = 5;
	private static final int PENTAD_SPREAD_DEGREES = 30;
	private static final int TRIAD_SPREAD_DEGREES = 20;

	//Globals
	private static float MOVE_DIST = type.strideRadius;
	private static float safeDist = 0;
	private static boolean bulletSafe;

	/**
	 * to call execute, number of enemies must be > 0
	 */
	public static void execute() throws GameActionException {
	    System.out.println("Instantiation:"+Clock.getBytecodeNum());

		//int temp = Clock.getBytecodeNum();
		//System.out.println("moving used: " + (Clock.getBytecodeNum() - temp));
		potentialAttackStats attack = chooseTargetAndShotType();
        System.out.println("Shot Calc:"+Clock.getBytecodeNum());

        if(attack == null){
			return;
		}
		BodyInfo target = attack.getTarget();
		if(target == null){
			//System.out.println("rip target");
			tryMoveDirection(here.directionTo(MapAnalysis.center), true, false);
			return;
		}
		System.out.println("My Target is"+attack.getTarget().getID());
		MapLocation targetLoc = target.getLocation();
		//Direction targetDir = here.directionTo(attack.getTarget().getLocation());
		//Direction moveDir = (targetDir);
		Direction moveDir = calcMoveDir(attack.getTarget());
        System.out.println("Move Calc:"+Clock.getBytecodeNum());

        if (moveDir != null) {
			MapLocation nextLoc = here.add(moveDir, type.strideRadius);;
			if (nextLoc.distanceTo(targetLoc) < here.distanceTo(targetLoc)) {
				//if(debug){System.out.println("moved before shooting");}
				rc.move(moveDir, MOVE_DIST);
			}
		}

		//If we've moved recalculate the shot type but not the shot
		if(rc.hasMoved()) {
			attack.setShotType(calculateShotType(attack.getTarget(),attack.getShotValue()));
		}
        System.out.println("Shot recalc:"+Clock.getBytecodeNum());

        parseShotTypeAndShoot(attack.getTarget(), attack.getShotType());

		System.out.println("I tried to shoot a "+ attack.getShotType());
		if(rc.getMoveCount()  == 0 && moveDir != null){
			//System.out.println("shot before moving");
			rc.move(moveDir, MOVE_DIST);
		}
	}

	//////////////////////////Movement Micro////////////////////////

	/**
	 * Picks the best direction to move in;
	 * @param target our eventual target
	 * @return the direction we want to move in
	 */
	private static Direction calcMoveDir(BodyInfo target) {
		MapLocation targetLoc = target.getLocation();
		Direction targetDir = here.directionTo(targetLoc);
		float maxDist = -99999;
		Direction backupDir = null;
		if(Math.abs(safeDist -1) < .01){//dealing with scout in tree
			System.out.println("Scout Hunting");
			MapLocation targetSpot = targetLoc.add(targetDir.opposite(), (float) 2.004);
			float dist = here.distanceTo(targetSpot);
			if(dist < type.strideRadius && rc.canMove(targetDir, dist)){
				MOVE_DIST = dist;
				return targetDir;
			}
		}
		//check for easy move in desired dir
		Direction dir = here.directionTo(targetLoc);
		if(rc.canMove(dir, type.strideRadius)){
			MapLocation moveTo = here.add(dir, type.strideRadius);
			if(isSafe(moveTo, target))
				return dir;
			else if (bulletSafe && moveTo.distanceTo(targetLoc) > maxDist){
				maxDist = moveTo.distanceTo(targetLoc);
				backupDir = dir;
			}
		}
        System.out.println("Checking for an easy move:"+Clock.getBytecodeNum());

        //check the other directions
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for(int i = 0; i < 18; i++){
            System.out.println("Going through directions:"+Clock.getBytecodeNum());

            if(rc.canMove(left, type.strideRadius)){
				MapLocation moveTo = here.add(left, type.strideRadius);
				if(isSafe(moveTo, target))
					return left;
				else if (bulletSafe && moveTo.distanceTo(targetLoc) > maxDist){
					maxDist = moveTo.distanceTo(targetLoc);
					backupDir = left;
				}
			}
			if(rc.canMove(right, type.strideRadius)){
				MapLocation moveTo = here.add(right, type.strideRadius);
				if(isSafe(moveTo, target))
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

	/**
	 * Checks if the loc is in danger and if it is in a safe spot relative to the target
	 * @param loc the location to check for safety
	 * @param target the eventual target whos location we want to check against
	 * @return whether that location is safe
	 */
	private static boolean isSafe(MapLocation loc, BodyInfo target) {
		//System.out.println("safe dist = " + safeDist);
		float safeDistLocal = 0;

        System.out.println("Pre Bullet:"+Clock.getBytecodeNum());

        //check that now bullets will hit the location
		bulletSafe = true;
		for (BulletInfo b : nearbyBullets) {
		    if(b.location.distanceTo(loc)>2){
		        break;
            }
			if (willCollide(b, loc)) {
				bulletSafe = false;
				return false;
			}
		}

		System.out.println("Post Bullet:"+Clock.getBytecodeNum());

        //check if the spot can be immediately damaged next turn
		if(Math.abs(safeDist) < .01){
            if(nearbyEnemyRobots.length == 0 || nearbyEnemyRobots.length == 1 && nearbyEnemyRobots[0].type == RobotType.GARDENER)
                return true;
			else{
				RobotInfo[] nearbyEs = rc.senseNearbyRobots(loc, -1, enemy); //robots closest to gardener or tree
				for(RobotInfo e: nearbyEs){
					if(e.type != RobotType.GARDENER){
						loc = e.location;
						safeDistLocal = e.type.bodyRadius + type.bodyRadius + e.type.strideRadius + (e.type == RobotType.LUMBERJACK ? GameConstants.LUMBERJACK_STRIKE_RADIUS : e.type.bulletSpeed);
						if(loc.distanceTo(target.getLocation()) < safeDistLocal){
							return false;
						}
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
		score -= 20 * (howFarAwayTheyCanGet > 0 ? howFarAwayTheyCanGet: 0);
		score += (robot.type == RobotType.GARDENER ? 20 : 0);
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
		RobotInfo targetRobot = null;
		if(target.isRobot()){
			targetRobot = (RobotInfo)target;
		}
		// come up with some sort of formula for choosing the kind of shot
        singleValue+=nearbyAlliedRobots.length;
		int pentadValue = singleValue;
		int triadValue = singleValue;
		if(target == null)
			return NO_SHOT;

		//Its better if we can also do collateral dmg to enemy trees
		Direction targetDir = here.directionTo(target.getLocation());
		for(TreeInfo t: nearbyEnemyTrees){
			Direction dirToT = here.directionTo(t.location);
			float deg = targetDir.degreesBetween(dirToT);
			if(deg < PENTAD_SPREAD_DEGREES){
				pentadValue += TREE_HIT_VALUE;
				if(deg < TRIAD_SPREAD_DEGREES){
					triadValue += TREE_HIT_VALUE;
				}
			}
		}

		//Its better if we can deal collateral dmg to other enemies
		for(RobotInfo r: nearbyEnemyRobots){
			float howFarAwayTheyCanGet = here.distanceTo(r.location)
					- type.bulletSpeed
					- type.bodyRadius
					- r.type.bodyRadius
					+ r.type.strideRadius;

			float valFromHitting = 18 - howFarAwayTheyCanGet * 2; //how did we get 7?
			if(valFromHitting < 0)
				break;
			Direction dirToR = here.directionTo(r.location);
			float deg = targetDir.degreesBetween(dirToR);
			if(deg < PENTAD_SPREAD_DEGREES){
				pentadValue += valFromHitting;
				if(deg < TRIAD_SPREAD_DEGREES){
					triadValue += valFromHitting;
				}
			}
		}

		//adjusting shots
		if(pentadValue == triadValue && pentadValue>110){
			pentadValue = -1;
			triadValue = 96;
		}
		if(target.isRobot() && targetRobot!=null && targetRobot.type == RobotType.ARCHON){
			return SINGLE_SHOT;
		}

		System.out.println("Pentad Value: " + pentadValue);
		System.out.println("Triad Value: " + triadValue);
		/*if (target != null) {
			if (here.distanceTo(target.getLocation()) - type.bulletSpeed - type.bodyRadius - target.getRadius() < 0) {
				score = 7;
			}
		}*/
		int treeMod = rc.getTreeCount()/3;
		if (pentadValue + treeMod> 110) {
			return PENTAD_SHOT;
		}
		if (triadValue + treeMod > 95) {
			return TRIAD_SHOT;
		}
		if (singleValue + treeMod > 69){
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


	///////////////////// These Might Belong in Util/////////////////////
	public static void shootSingleShot(BodyInfo target) throws GameActionException {
		if (rc.canFireSingleShot() && target != null) {
			rc.fireSingleShot(rc.getLocation().directionTo(target.getLocation()));
			System.out.println("I shot a single");

		}

	}

	public static void shootTriadShot(BodyInfo target) throws GameActionException {
		System.out.println(target == null);

		if (rc.canFireTriadShot() && target != null) {
			rc.fireTriadShot(rc.getLocation().directionTo(target.getLocation()));
			System.out.println("I shot a triad");

		}

	}

	public static void shootPentadShot(BodyInfo target) throws GameActionException {
		System.out.println(target == null);

		if (rc.canFirePentadShot() && target != null) {
			rc.firePentadShot(rc.getLocation().directionTo(target.getLocation()));
			System.out.println("I shot a pentad");

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