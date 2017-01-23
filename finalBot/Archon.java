package team008.finalBot;

import battlecode.common.*;

public class Archon extends Bot {

	public Archon(RobotController r) throws GameActionException {
		super(r);
		// anything else archon specific
	}

	

	public static int unitsBuilt = 0;
	public static boolean inDistress = false;
	public void takeTurn() throws Exception {
		
		if(nearbyEnemyRobots.length>0){
			if(!inDistress){
			Message.ARCHON_DISTRESS_NUM.setValue(Message.ARCHON_DISTRESS_NUM.getValue()+1);
			inDistress = true;
			}
			runAway();
		}
		else{
			if(inDistress){

				Message.ARCHON_DISTRESS_NUM.setValue(Message.ARCHON_DISTRESS_NUM.getValue()-1);
				inDistress = false;
			}
		}
		if (Message.ARCHON_BUILD_NUM.getValue() > 0 && rc.getTeamBullets() > (100 + 
				(inDistress ? 
						((Message.ARCHON_DISTRESS_NUM.getValue() < MapAnalysis.initialAlliedArchonLocations.length) ? 
								10 : nearbyEnemyRobots.length)
						: (MapAnalysis.initialAlliedArchonLocations.length == 1 ? 0 : unitsBuilt * 2)))) {
			hireGardener();
			unitsBuilt++;
		}

	}

	public void hireGardener() throws GameActionException {
		Direction dir = here.directionTo(MapAnalysis.center);
		if (rc.canHireGardener(dir)) {
			rc.hireGardener(dir);
			Message.ARCHON_BUILD_NUM.setValue(Message.ARCHON_BUILD_NUM.getValue()-1);
			Message.NUM_GARDENERS.setValue(Message.NUM_GARDENERS.getValue() + 1);
			return;
		}
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for (int i = 18; i-- > 0;) {
			if (rc.canHireGardener(left)) {
				rc.hireGardener(left);
				Message.ARCHON_BUILD_NUM.setValue(Message.ARCHON_BUILD_NUM.getValue()-1);
				Message.NUM_GARDENERS.setValue(Message.NUM_GARDENERS.getValue() + 1);

				return;
			}
			if (rc.canHireGardener(right)) {
				rc.hireGardener(right);
				Message.ARCHON_BUILD_NUM.setValue(Message.ARCHON_BUILD_NUM.getValue()-1);
				Message.NUM_GARDENERS.setValue(Message.NUM_GARDENERS.getValue() + 1);
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