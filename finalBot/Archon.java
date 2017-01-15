package team008.finalBot;

import battlecode.common.*;

public class Archon extends Bot {

	public Archon(RobotController r) throws GameActionException {
		super(r);
		// anything else archon specific
	}

	

	public static int unitsBuilt = 0;

	public void takeTurn() throws Exception {
		//dirIAmMoving = findOpenSpaces();
		if (rc.getTeamBullets() > 100 + ((rc.readBroadcast(4) > 1) ? unitsBuilt * 10 : 0)
				&& rc.readBroadcast(13) > 0) {
			hireGardener();
			unitsBuilt++;
		}
		if(nearbyEnemyRobots.length > 0){
				notifyFriendsOfEnemies(nearbyEnemyRobots);
		}


	}

	public void hireGardener() throws GameActionException {
		// System.out.println("Trying to hire a gardener");
		Direction dir = here.directionTo(MapAnalysis.center);
		for (int i = 36; i-- > 0;) {
			if (rc.canHireGardener(dir)) {
				rc.hireGardener(dir);
				rc.broadcast(13, rc.readBroadcast(13) - 1);
				// System.out.println("Hired a gardener!");
				return;
			} else {
				dir = dir.rotateLeftDegrees(10);
			}
		}
	}

	private static double wallModCalc(MapLocation retreatLoc, Direction dir) throws GameActionException {
		double mod = 0;
		while (here.distanceTo(retreatLoc) < type.sensorRadius && rc.onTheMap(retreatLoc)) {
			retreatLoc = retreatLoc.add(dir);
			mod += 1.0;

		}
		return mod;

	}

	public void runAway(RobotInfo[] enemies) throws GameActionException {
		Direction bestRetreatDir = null;
		double bestValue = -10000;
		int count = 0;
		Direction dir = new Direction(0);

		while (count < 36) {

			MapLocation retreatLoc = here.add(dir, rc.getType().strideRadius);
			RobotInfo closestEnemy = Util.closestRobot(enemies, retreatLoc);

			float dist = retreatLoc.distanceTo(closestEnemy.location);
			double allyMod = RangedCombat.numOtherAlliesInSightRange(here.add(dir, rc.getType().strideRadius));
			double wallMod = wallModCalc(retreatLoc, dir);

			if (dist + allyMod + wallMod > bestValue) {
				bestValue = dist + allyMod + wallMod;
				bestRetreatDir = dir;
			}
			count++;
			dir = dir.rotateRightDegrees(10);

		}

		if (!rc.hasMoved()) {
			if (bestRetreatDir != null) {
				goTo(bestRetreatDir);
			}
			goTo(Util.randomDirection());
		}

	}
}