package team008.finalBot;
import battlecode.common.*;

public class Archon extends Bot {
	private static int turnsTryingToReach = 0;
	private static boolean initialBuilder;
	private static int roundIBecameBuilder;

	public Archon(RobotController r) throws GameActionException {
		super(r);
		initialBuilder = false;
		//System.out.println("here");
		// anything else archon specific
	}

	

	public static int unitsBuilt = 0;
	public static boolean inDistress = false;
	public void takeTurn() throws Exception {
		if(roundNum == 1 && Message.INITIAL_BUILDER_HERE.getValue() == 0){
			//System.out.println("conflict dist = " + MapAnalysis.conflictDist);
			float myConflictDist = here.distanceTo(Util.closestLocation(MapAnalysis.initialEnemyArchonLocations));
			//System.out.println("my conflict dist = " + myConflictDist);
			if(Math.abs(myConflictDist - Message.CONFLICT_DIST.getFloatValue()) < 0.1){
				if(!willTrapOurselvesIn() && canIHire() || MapAnalysis.initialEnemyArchonLocations.length == 1){
					initialBuilder = true;
					roundIBecameBuilder = roundNum;
				}
			}
		}
		
		if(initialBuilder){
			if(debug)System.out.println("i am initial builder");
			if(Message.NUM_GARDENERS.getValue() == 0 && roundNum - roundIBecameBuilder > 0 && Message.NUM_ARCHONS.getValue() > 1){
				initialBuilder = false;
			}
			else{
				Message.INITIAL_BUILDER_HERE.setValue(roundNum);
			}
		}
		else if (canIHire() && roundNum - Message.INITIAL_BUILDER_HERE.getValue() > 1){
			initialBuilder = true;
			roundIBecameBuilder = roundNum;
			Message.INITIAL_BUILDER_HERE.setValue(roundNum);
		}
		
		if(nearbyEnemyRobots.length > 0 && !(nearbyEnemyRobots.length == 1 && (nearbyEnemyRobots[0].type == RobotType.GARDENER || nearbyEnemyRobots[0].type == RobotType.ARCHON))){
			if(!inDistress){
			Message.ARCHON_DISTRESS_NUM.setValue(Message.ARCHON_DISTRESS_NUM.getValue()+1);
			inDistress = true;
			}
			if(Message.NUM_ARCHONS.getValue() > 1){
				initialBuilder = false;
				Message.INITIAL_BUILDER_HERE.setValue(0);
			}
			runAway();
		}
		else{
			if(inDistress){
				Message.ARCHON_DISTRESS_NUM.setValue(Message.ARCHON_DISTRESS_NUM.getValue()-1);
				inDistress = false;
			}
		}
		if(rc.getMoveCount() == 0 && willTrapOurselvesIn()) {
            if(debug){System.out.println("I think we're gonna get trapped");}
            if(Message.NUM_GARDENERS.getValue() == 0){
                if(debug){System.out.println("make dat room doe");}
                makeRoomForGardener();
            }
            if(!rc.hasMoved()) {
                runAway();
            }
        }
		if(rc.getMoveCount() == 0){
            if(debug){System.out.println("I think im making room");}
            clearRoom();
		}
		if (initialBuilder && (Message.NUM_GARDENERS.getValue() == 0 || 
				(Message.ARCHON_BUILD_NUM.getValue() > 0 && rc.getTeamBullets() > (100 + 
				(inDistress ? 
						((Message.ARCHON_DISTRESS_NUM.getValue() < Message.NUM_ARCHONS.getValue()) ? 
								10 : nearbyEnemyRobots.length)
						: (MapAnalysis.initialAlliedArchonLocations.length == 1 ? 0 : unitsBuilt * 2)))))) {
			if (!willTrapOurselvesIn() || roundNum > 3) {
				if (Message.NUM_GARDENERS.getValue()
						- Message.GARDENER_TRAPPED_NUM.getValue() <= ((Message.NUM_GARDENERS.getValue() > 4 )? 1 : 0)) {
					System.out.println("We have " + Message.GARDENER_TRAPPED_NUM.getValue() + "/" + Message.NUM_GARDENERS.getValue() + " gardeners" );
					hireGardener();
					unitsBuilt++;
				}
			}
		}

	}
    private void makeRoomForGardener() throws GameActionException {

        if(nearbyNeutralTrees.length >= 1) {
            goTo(here.directionTo(nearbyNeutralTrees[0].location).opposite());
        } else if(nearbyAlliedTrees.length >= 1){
            goTo(here.directionTo(nearbyAlliedTrees[0].location).opposite());
        }

    }

	private boolean canIHire() {
		Direction dir = here.directionTo(MapAnalysis.center);
		if (rc.canHireGardener(dir)) {
			return true;
		}
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for (int i = 18; i-- > 0;) {
			if (rc.canHireGardener(left)) {
				return true;
			}
			if (rc.canHireGardener(right)) {
				return true;
			}
			left = left.rotateLeftDegrees(10);
			right = right.rotateRightDegrees(10);
		}
		return false;
	}

	private boolean willTrapOurselvesIn() throws GameActionException {
		int directionsWereScrewedIn = 0;
		Direction dir = new Direction(0);
		for(int i = 0; i < 4; i++){
			if(!rc.onTheMap(here.add(dir,4))){
					directionsWereScrewedIn++;
			}
			dir = dir.rotateLeftDegrees(90);
		}
		return directionsWereScrewedIn > 1;
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
		else{
			dir = determineInitialBuildDir();
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

	private Direction determineInitialBuildDir() throws GameActionException {
		Direction bestDir = closestCardinalDirection(here.directionTo(MapAnalysis.center));
		Direction dir = bestDir;
		float distToObstacle = calcDistToObstacle(dir);
		float highestDistToObstacle = distToObstacle;
		Direction left = dir.rotateLeftDegrees(45);
		Direction right = dir.rotateRightDegrees(45);
	    for(int i = 0; i < 8; i++){
	    	distToObstacle = calcDistToObstacle(left);
	    	if(distToObstacle > highestDistToObstacle){
	    		highestDistToObstacle = distToObstacle;
	    		bestDir = left;
	    	}
	    	left = left.rotateLeftDegrees(45);
	    	distToObstacle = calcDistToObstacle(right);
	    	if(distToObstacle > highestDistToObstacle){
	    		highestDistToObstacle = distToObstacle;
	    		bestDir = right;
	    	}
	    	right = right.rotateRightDegrees(45);
	    }
	    return bestDir;
	}

	private Direction closestCardinalDirection(Direction dir) throws GameActionException {
		Direction ret = new Direction(((int)(dir.getAngleDegrees()) / 45) * 45);
		return ret;
	}

	private float calcDistToObstacle(Direction dir) throws GameActionException {
		MapLocation spawnLoc = here.add(dir, type.bodyRadius + RobotType.GARDENER.bodyRadius);
		float distToEdge = calcEdgeDist(spawnLoc) - (float)(0.5);
		TreeInfo[] trees = rc.senseNearbyTrees(spawnLoc, -1, Team.NEUTRAL);
		float distToTree = -1;
		if(trees.length > 0)
			distToTree = trees[0].location.distanceTo(spawnLoc);
		if(distToEdge > 9000 && distToTree == -1){
			return 999;
		}
		else if (distToEdge > 9000){
			return distToTree;
		}
		else if (distToTree == -1){
			return distToEdge;
		}
		return (distToEdge < distToTree ? distToEdge: distToTree);
	}

	private float calcEdgeDist(MapLocation spawnLoc) throws GameActionException {
		Direction dir = new Direction(0);
		float bestEdgeDist = 9001;
		float edgeDist;
		for(int i = 0; i < 4; i++){
			MapLocation edge = Scout.checkForEdge(spawnLoc, dir);
			if(edge != null){
				edgeDist = spawnLoc.distanceTo(edge);
				if(edgeDist <  bestEdgeDist){
					bestEdgeDist = edgeDist;
				}
			}
			dir = dir.rotateLeftDegrees(90);
		}
		return bestEdgeDist;
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