package team008.soldierBot00;

import battlecode.common.*;

public class Archon extends Bot {
	public static int unitsBuilt = 0;
	public Archon(RobotController r) throws GameActionException {
		super(r);
		// anything else archon specific
	}

	public void takeTurn() throws Exception {
		if (rc.getTeamBullets() > 100 + unitsBuilt *2 && rc.readBroadcast(13)> 0) {
			hireGardener();
			unitsBuilt++;
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
}