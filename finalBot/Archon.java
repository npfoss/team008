package team008.finalBot;

import battlecode.common.*;

public class Archon extends Bot {
	private static int turnsTryingToReach = 0;

	public Archon(RobotController r) throws GameActionException {
		super(r);
		//System.out.println("here");
		// anything else archon specific
	}

	

	public static int unitsBuilt = 0;
	public static boolean inDistress = false;
	public void takeTurn() throws Exception {
		if(roundNum == 2 && Message.CLEAR_TREES_PLEASE.getLength() == 0){
			for(MapLocation m: rc.getInitialArchonLocations(enemy)){
				Message.ENEMY_ARMIES.addLocation(m);
			}
			if(tryMoveDirection(new Direction(0), false, true)){
				for(TreeInfo t: nearbyNeutralTrees){
					if(here.distanceTo(t.location) - t.radius < 4)
						Message.CLEAR_TREES_PLEASE.addLocation(t.location);
				}
				//not surrounded
			}
		}
		
		if(nearbyEnemyRobots.length > 0 && !(nearbyEnemyRobots.length == 1 && (nearbyEnemyRobots[0].type == RobotType.GARDENER || nearbyEnemyRobots[0].type == RobotType.ARCHON))){
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
						((Message.ARCHON_DISTRESS_NUM.getValue() < Message.NUM_ARCHONS.getValue()) ? 
								10 : nearbyEnemyRobots.length)
						: (MapAnalysis.initialAlliedArchonLocations.length == 1 ? 0 : unitsBuilt * 2)))) {
			hireGardener();
			unitsBuilt++;
		}
		if(rc.getMoveCount() == 0){
			clearRoom();
		}
	}

	private void clearRoom() throws GameActionException {
	    if(Message.GARDENER_BUILD_LOCS.getLength() > 0){
	    	MapLocation closestLoc = Message.GARDENER_BUILD_LOCS.getClosestLocation(here);
	    	if(debug)rc.setIndicatorLine(here, closestLoc, 255, 0, 0);
	    	target = closestLoc.add(closestLoc.directionTo(here), (float) (type.bodyRadius + RobotType.GARDENER.bodyRadius + 0.01));
	    	if(here.distanceTo(target) > 0.01 && Util.closestSpecificType(rc.senseNearbyRobots(closestLoc, 7, us), here, RobotType.GARDENER) == null){
	    		goTo(target);
	    		turnsTryingToReach++;
	    	}
	    }
	    else{
	    	explore();
	    }
	}

	public void hireGardener() throws GameActionException {
		Direction dir = here.directionTo(MapAnalysis.center);
		MapLocation gardenerBuildLoc = Message.GARDENER_BUILD_LOCS.getClosestLocation(here);
		if(gardenerBuildLoc != null)
			dir = here.directionTo(Message.GARDENER_BUILD_LOCS.getClosestLocation(here));
		else if(nearbyTrees.length > 0){
			dir = here.directionTo(nearbyTrees[0].location).opposite();
		}
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