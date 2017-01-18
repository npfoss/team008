package team008.finalBot;

import battlecode.common.*;

public class RangedCombat extends Bot {

	private static final String SINGLE_SHOT = "single shot";
	private static final String TRIAD_SHOT = "triad shot";
	private static final String PENTAD_SHOT = "pentad shot";
	private static final int WORTHWHILE_SHOT_THRESHOLD = 50;

	/*
	 * to call execute, number of enemies must be > 0
	 */
	public static void execute() throws GameActionException {

		//int temp = Clock.getBytecodeNum();
		tryMoveDirection(new Direction(here,MapAnalysis.center), false);
		//System.out.println("moving used: " + (Clock.getBytecodeNum() - temp));
		potentialAttackStats attack = chooseTargetAndShotType();
		if (calculatedMove != null) {
			MapLocation tempLoc = here;
			here = here.add(calculatedMove, type.strideRadius);
			potentialAttackStats attack2 = chooseTargetAndShotType();

			if (attack2.getShotValue() > attack.getShotValue() || attack.getTarget() != null && tempLoc.directionTo(attack.getTarget().getLocation())
					.radiansBetween(calculatedMove) < Math.PI / 2) {
				rc.move(calculatedMove, type.strideRadius);
				attack = attack2;
			} else {
				here = tempLoc;
			}
		}
		if (worthShooting(attack.getShotValue())) {
			parseShotTypeAndShoot(attack.getTarget(), attack.getShotType());
		}
		if(rc.getMoveCount()  == 0 && calculatedMove != null){
			rc.move(calculatedMove, type.strideRadius);
		}

	}

	/**
	 * Tries to asses if its work shooting.
	 *
	 * @return
	 */
	private static boolean worthShooting(int shotValue) {
		return shotValue > WORTHWHILE_SHOT_THRESHOLD;
	}

	///////////////////// Shooting and Target Micro/////////////////////

	/**
	 * Picks the target
	 *
	 * @return
	 * @throws GameActionException
	 */
	private static potentialAttackStats chooseTargetAndShotType() throws GameActionException {
		int score;
		int shotValue = 0;
		int bestScore = -999999;
		RobotInfo bestRobot = null;
		int canWeHitThemValue;
		int robotsToCalculate = 2;
		int calculated = 0;
		for (RobotInfo robot : nearbyEnemyRobots) {
			canWeHitThemValue = canWeHitHeuristic(robot);
			score = (int) (-robot.getHealth() + robot.getType().attackPower + canWeHitThemValue);

			if (score > bestScore && isDirectionSafe(robot)) {
				bestScore = score;
				bestRobot = robot;
				shotValue = canWeHitThemValue;
			}
			calculated++;
			if (calculated == robotsToCalculate){
				break;
			}
		}

		return new potentialAttackStats(bestRobot, calculateShotType(bestRobot), shotValue);
	}

	/**
	 * An attempt to take shots we can make
	 *
	 * @param robot
	 * @return
	 */
	public static int canWeHitHeuristic(RobotInfo robot) {
		int score = 100;
		float howFarAwayTheyCanGet = here.distanceTo(robot.location) - type.bulletSpeed - type.bodyRadius
				- robot.type.bodyRadius + robot.type.strideRadius;
		//score -= 25 * howFarAwayTheyCanGet / (nearbyTrees.length + 1);
		score -= 25 * howFarAwayTheyCanGet;
		score += 10 * nearbyEnemyRobots.length;
		return score;
	}

	/**
	 * Picks whether to shot 1,3 or 5 bullets.
	 *
	 * @return the shot type
	 * @throws GameActionException
	 */
	public static String calculateShotType(BodyInfo target) throws GameActionException {
		// come up with some sort of formula for choosing the kind of shot
		float score = nearbyEnemyRobots.length;
		if (target != null) {
			if (here.distanceTo(target.getLocation()) - type.bulletSpeed - type.bodyRadius - target.getRadius() < 0) {
				score = 7;
			}
		}
		if (score > 6) {
			return PENTAD_SHOT;
		}
		if (score > 3) {
			return TRIAD_SHOT;
		}
		return SINGLE_SHOT;
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

		public potentialAttackStats(BodyInfo target, String shotType, int shotValue) {
			this.target = target;
			this.shotType = shotType;
			this.shotValue = shotValue;
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
	}
}