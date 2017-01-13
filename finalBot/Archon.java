package team008.finalBot;

import battlecode.common.*;

public class Archon extends Bot {

	public Archon(RobotController r) throws GameActionException {
		super(r);
		// anything else archon specific
	}

	public static Direction findOpenSpaces() throws GameActionException {
		int spaces = 0;
		Direction dir = new Direction(0);
		float xavg = 0;
		float yavg = 0;
		for (int i = 0; i < 7; i++) {
			boolean isOkay = true;
					
			for (TreeInfo t : nearbyNeutralTrees)
				if (dir.radiansBetween(here.directionTo(t.getLocation())) < Math.PI / 4) {
					isOkay = false;
					break;
				}
			for (TreeInfo t : nearbyAlliedTrees)
				if (dir.radiansBetween(here.directionTo(t.getLocation())) < Math.PI / 4) {
					isOkay = false;
					break;
				}
			for (TreeInfo t : nearbyEnemyTrees)
				if (dir.radiansBetween(here.directionTo(t.getLocation())) < Math.PI / 4) {
					isOkay = false;
					break;
				}
			for (RobotInfo t : nearbyRobots)
				if (dir.radiansBetween(here.directionTo(t.getLocation())) < Math.PI / 4) {
					isOkay = false;
					break;
				}
			
			if (rc.canMove(dir, type.sensorRadius) & isOkay && rc.onTheMap(here.add(dir,type.sensorRadius-1))) {
				MapLocation temp = here.add(dir, type.sensorRadius);
				xavg += temp.x;
				yavg += temp.y;
				spaces++;
			}
			dir = dir.rotateLeftDegrees(360/7);
		}

		MapLocation temp = new MapLocation(xavg / spaces, yavg / spaces);
		if (temp.distanceTo(here) < .001) {
			return here.directionTo(MapAnalysis.center);
		}
		return here.directionTo(new MapLocation(xavg / spaces, yavg / spaces));

	}

	public void takeTurn() throws Exception {
		dirIAmMoving = findOpenSpaces();
		if (rc.getRoundNum() + 5 > GameConstants.GAME_DEFAULT_ROUNDS
				|| rc.getTeamVictoryPoints() + rc.getTeamBullets() / 10 > 1000) {
			rc.donate(((int) (rc.getTeamBullets() / 10)) * 10);
		} else if (rc.getTeamBullets() > 100 & rc.readBroadcast(13) > 0) {
			System.out.println("I must build Unit Type: Gardener:" + rc.readBroadcast(13));
			hireGardener();
		}

		RobotInfo[] enemies = rc.senseNearbyRobots(-1, enemy);
		RobotInfo[] allies = rc.senseNearbyRobots(-1, us);

//		if (enemies.length > 0) {
//			rc.setIndicatorDot(here, 0, 255, 0);
//			runAway(enemies, allies);
//		}
		if (rc.getMoveCount() == 0) {
			goTo(dirIAmMoving);
		}
	}

	public void hireGardener() throws GameActionException {
		System.out.println("Trying to hire a gardener");
		Direction dir = dirIAmMoving.opposite();
		for (int i = 36; i-- > 0;) {
			if (rc.canHireGardener(dir)) {
				rc.hireGardener(dir);
				rc.broadcast(13, rc.readBroadcast(13) - 1);
				System.out.println("Hired a gardener!");
				break;
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

	public void runAway(RobotInfo[] enemies, RobotInfo[] allies) throws GameActionException {
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

		if (bestRetreatDir != null) {
			goTo(bestRetreatDir);
		}
		goTo(Util.randomDirection());
	}
}