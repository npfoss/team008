package team008.finalBot;

import battlecode.common.*;

public class Archon extends Bot {

	public Archon(RobotController r) throws GameActionException {
		super(r);
		// anything else archon specific
	}

	

	public static int unitsBuilt = 0;

	public void takeTurn() throws Exception {
		if (rc.getTeamBullets() > 100 + unitsBuilt *2 && rc.readBroadcast(13)> 0) {
			hireGardener();
			unitsBuilt++;
		}
		if(nearbyEnemyRobots.length>0){
			runAway();
		}

	}

	public void hireGardener() throws GameActionException {
		Direction dir = here.directionTo(MapAnalysis.center);
		if (rc.canHireGardener(dir)) {
			rc.hireGardener(dir);
			rc.broadcast(13, rc.readBroadcast(13) - 1);
			rc.broadcast(5, rc.readBroadcast(5)+1);
			return;
		}
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for (int i = 18; i-- > 0;) {
			if (rc.canHireGardener(left)) {
				rc.hireGardener(left);
				rc.broadcast(13, rc.readBroadcast(13) - 1);
				rc.broadcast(5, rc.readBroadcast(5)+1);

				return;
			}
			if (rc.canHireGardener(right)) {
				rc.hireGardener(right);
				rc.broadcast(13, rc.readBroadcast(13) - 1);
				rc.broadcast(5, rc.readBroadcast(5)+1);
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

}