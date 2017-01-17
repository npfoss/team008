package team008.finalBot;

import battlecode.common.*;

public class Archon extends Bot {

	public Archon(RobotController r) throws GameActionException {
		super(r);
		// anything else archon specific
	}

	

	public static int unitsBuilt = 0;

	public void takeTurn() throws Exception {
		//quick method of somewhat alternating which archon builds the gardener
		if (rc.getTeamBullets() > 100 + ((rc.readBroadcast(4) > 1) ? unitsBuilt * 10 : 0)
				&& rc.readBroadcast(13) > 0) {
			hireGardener();
			unitsBuilt++;
			rc.broadcast(22, unitsBuilt);
		}
		if (nearbyEnemyRobots.length > 0) {
			Messaging.sendDistressSignal(here);
			runAway();
	}

	}
	
	public void hireGardener() throws GameActionException {
		// System.out.println("Trying to hire a gardener");
		Direction dir = here.directionTo(MapAnalysis.center);
		if (rc.canHireGardener(dir)) {
			rc.hireGardener(dir);
			rc.broadcast(13, rc.readBroadcast(13) - 1);
			return;
		}
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for (int i = 18; i-- > 0;) {
			if (rc.canHireGardener(left)) {
				rc.hireGardener(left);
				rc.broadcast(13, rc.readBroadcast(13) - 1);
				return;
			}
			if (rc.canHireGardener(right)) {
				rc.hireGardener(right);
				rc.broadcast(13, rc.readBroadcast(13) - 1);
				return;
			}
			left = left.rotateLeftDegrees(10);
			right = right.rotateRightDegrees(10);
		}
	}
public static void runAway() throws GameActionException{
	Direction dir = new Direction(0);
	int thingsInTheWay = 0;
	int bestScore = 10000;
	Direction bestDir = new Direction(0);
	for (int i = 0; i < 16; i++) {
		if (!rc.onTheMap(here.add(dir, (float) (type.sensorRadius - .001)))) {
			thingsInTheWay += 100;
		}
		
		for (RobotInfo t : nearbyRobots)
			if (Util.isDangerous(t.type)&& dir.radiansBetween(here.directionTo(t.location)) < Math.PI / 2) {
				thingsInTheWay += (t.team == us) ? -10*t.type.attackPower : 10*t.type.attackPower;
			}
		if (thingsInTheWay < bestScore) {
			bestDir = dir;
			bestScore = thingsInTheWay;
		}
			dir = dir.rotateLeftDegrees((float) 22.5);
			thingsInTheWay = 0;
		}
		if (bestScore != 10000) {
			tryMoveDirectionDangerous(bestDir);
		}
}
//Used too much bytecode, incorporated into runAway
//	private static double wallModCalc(MapLocation retreatLoc, Direction dir) throws GameActionException {
//		double mod = 0;
//		while (here.distanceTo(retreatLoc) < type.sensorRadius && rc.onTheMap(retreatLoc)) {
//			retreatLoc = retreatLoc.add(dir);
//			mod += 1.0;
//		}
//		return mod;
//	}
//	public void runAway2(RobotInfo[] enemies) throws GameActionException {
//		Direction bestRetreatDir = null;
//		double bestValue = -10000;
//		int count = 0;
//		Direction dir = new Direction(0);
//		while (count < 36) {
//			MapLocation retreatLoc = here.add(dir,type.strideRadius);
//			RobotInfo closestEnemy = Util.closestRobot(enemies, retreatLoc);
//			float dist = retreatLoc.distanceTo(closestEnemy.location);
//			double allyMod = RangedCombat.numOtherAlliesInSightRange(here.add(dir, rc.getType().strideRadius));
//			double wallMod = wallModCalc(retreatLoc, dir);
//			if (dist + allyMod + wallMod > bestValue) {
//				bestValue = dist + allyMod + wallMod;
//				bestRetreatDir = dir;
//			}
//			count++;
//			dir = dir.rotateRightDegrees(10);
//		}
//		if (!rc.hasMoved()) {
//			if (bestRetreatDir != null) {
//				goTo(bestRetreatDir);
//			}
//			goTo(Util.randomDirection());
//		}
//	}
}