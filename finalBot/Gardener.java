package team008.finalBot;
import battlecode.common.*;

public class Gardener extends Bot {
	public boolean isExploring;
	public static Direction dirIAmMoving;
	public static boolean updatedLocs;
	public static int turnsIHaveBeenTrying;
	//public static boolean tankBuilder;
	//public static int turnsTank;
    public static float patienceVal;

	public Gardener(RobotController r) throws GameActionException {
		super(r);
		isExploring = true;
		updatedLocs = false;
		//tankBuilder = false;
		turnsIHaveBeenTrying = 0;
		//turnsTank = 0;
        patienceVal = 1;

        // anything else gardener specific
	}

	private static Direction findOpenSpaces() throws GameActionException {
		// TODO: make this better
		Direction dir = new Direction(0);
		int thingsInTheWay = 0;
		int bestScore = 10000;
		Direction bestDir = new Direction(0);
		for (int i = 0; i < 16; i++) {
			if (!rc.onTheMap(here.add(dir, (float) (type.sensorRadius - .001)))) {
				thingsInTheWay += 10;
			}
			for (TreeInfo t : nearbyAlliedTrees)
				if (Math.abs(dir.radiansBetween(here.directionTo(t.location))) < Math.PI / 2) {
					thingsInTheWay += 2;
				}
			boolean addedTree = false;
			for (TreeInfo t : nearbyNeutralTrees){
				if(here.distanceTo(t.location) < 6 + t.radius && !addedTree){
					Message.CLEAR_TREES_PLEASE.addLocation(t.location);
					addedTree = true;
				}
				if (Math.abs(dir.radiansBetween(here.directionTo(t.location))) < Math.PI / 2){
					thingsInTheWay += 2;
				}
			}
			for (RobotInfo t : nearbyRobots)
				if ((t.type == RobotType.ARCHON || t.type == RobotType.GARDENER)
						&& Math.abs(dir.radiansBetween(here.directionTo(t.location))) < Math.PI / 2) {
					thingsInTheWay += (t.type == RobotType.ARCHON ? 2 : 10);
				}
			if (thingsInTheWay < bestScore) {
				bestDir = dir;
				bestScore = thingsInTheWay;
			}
			// rc.setIndicatorDot(here.add(dir), thingsInTheWay*10,
			// thingsInTheWay*10, thingsInTheWay*10);
			// System.out.println("ThisScore: " + thingsInTheWay);
			// System.out.println(dir.toString());
			dir = dir.rotateLeftDegrees((float) 22.5);
			thingsInTheWay = 0;
		}
		// System.out.println("Best Score: " + bestScore);
		// System.out.println(bestDir.toString());

		return bestDir;

	}
	public boolean isBadLocation(MapLocation targetLoc) throws GameActionException{
		if(targetLoc == null){
			return false;
		}
		if(turnsIHaveBeenTrying > 30){
			turnsIHaveBeenTrying = 0;
			Message.GARDENER_BUILD_LOCS.removeLocation(targetLoc);
			return true;
		}
		if(rc.canSenseLocation(targetLoc) && rc.senseRobotAtLocation(targetLoc) != null && rc.senseRobotAtLocation(targetLoc).type != RobotType.GARDENER && rc.senseRobotAtLocation(targetLoc).type != RobotType.ARCHON || !rc.canSenseLocation(targetLoc)){
			turnsIHaveBeenTrying++;
			return false;
		}
		float dist = here.distanceTo(targetLoc);
		if(
		(dist < type.sensorRadius -.001 && (!rc.onTheMap(targetLoc) || (rc.canSenseAllOfCircle(targetLoc, type.bodyRadius) && rc.isCircleOccupiedExceptByThisRobot(targetLoc, type.bodyRadius))) 
		|| (!rc.onTheMap(here.add(here.directionTo(targetLoc), (float)(dist + (type.sensorRadius -.001 - dist < 2 ? type.sensorRadius -.001 - dist : 2))))
		&& Message.GARDENER_BUILD_LOCS.getLength() > 1))){
			turnsIHaveBeenTrying = 0;
			Message.GARDENER_BUILD_LOCS.removeLocation(targetLoc);
			return true;
		}
		return false;
	}
	public void updateLocs() throws GameActionException{
		for(int i = 0; i < 6 ; i++)
		Message.GARDENER_BUILD_LOCS.addLocation(here.add(new Direction((float) (Math.PI/3 * i)), (float) 8.5 / patienceVal));
	}


	public void takeTurn() throws GameActionException {
		/*
		if(debug){
		if(tankBuilder)
			System.out.println("tank builder");
			System.out.println("dtc = " + here.distanceTo(MapAnalysis.center));
			System.out.println("rb = " + Message.DIST_TO_CENTER.getFloatValue());
		}
		if(tankBuilder && Math.abs(here.distanceTo(MapAnalysis.center) - Message.DIST_TO_CENTER.getFloatValue()) > 0.1){
			tankBuilder = false;
			if(debug)System.out.println("not tank builder");
		}
		if(tankBuilder && rc.getHealth() < 9){//announce my death
			Message.DIST_TO_CENTER.setValue((float)(999));
		}
		if(tankBuilder && turnsTank > 25){
			tankBuilder = false;
			Message.DIST_TO_CENTER.setValue((float)(999));
		}*/
		waterLowestHealthTree();
		if (nearbyEnemyRobots.length > 0) {
			//System.out.println("sent target d");
			Message.DISTRESS_SIGNALS.addLocation(nearbyEnemyRobots[0].location);
		}
		if(nearbyBullets.length > 0 && rc.senseNearbyTrees(2, us).length == 0){
			MapLocation moveTo = here.add(here.directionTo(nearbyBullets[0].location).opposite(), type.strideRadius);
			RangedCombat.bulletMove(moveTo, true);
		}
		else if (isExploring) {
			MapLocation targetLoc = Message.GARDENER_BUILD_LOCS.getClosestLocation(here);
			while (isBadLocation(targetLoc)) {
				targetLoc = Message.GARDENER_BUILD_LOCS.getClosestLocation(here);
			}
			if (targetLoc == null) {
				if (dirIAmMoving == null || myRand.nextDouble() < .5
						+ (double) (-rc.getRoundNum()) / (double) (2 * rc.getRoundLimit())) {
					dirIAmMoving = findOpenSpaces();
				}
				goTo(dirIAmMoving);
				boolean farAway = true;
				for (RobotInfo r : nearbyAlliedRobots) {
					if (r.type == RobotType.GARDENER || r.type == RobotType.ARCHON) { //shouldnt be hard set
						farAway = false;
						break;
					}
				}
				isExploring = !farAway;
			} else {
	        	if(debug)rc.setIndicatorLine(here, targetLoc, (us == Team.A ? 255: 0), (us == Team.A ? 0: 255), 0); 
				goTo(targetLoc);
				if (here.distanceTo(targetLoc) < .1) {
					isExploring = false;
				}
				if(here.distanceTo(targetLoc) > 20){
					boolean farAway = true;
					for (RobotInfo r : nearbyAlliedRobots) {
						if (r.type == RobotType.GARDENER || r.type == RobotType.ARCHON) {
							farAway = false;
							break;
						}
					}
					isExploring = !farAway;
				}
			}
			if (Message.NUM_GARDENERS.getValue() == 1) {
				isExploring = false;
			}
			/*
			if(!isExploring && Message.NUM_GARDENERS.getValue() > 1){
				//if(debug)rc.setIndicatorLine(here, MapAnalysis.center, 255, 0, 0);
				float dtc = Message.DIST_TO_CENTER.getFloatValue();
				if(here.distanceTo(MapAnalysis.center) < dtc || dtc == 0){
					if(debug)System.out.println("tank builder");
					tankBuilder = true;	
					Message.DIST_TO_CENTER.setValue(here.distanceTo(MapAnalysis.center));
				}
			}*/
		}
		if (!isExploring
				|| nearbyEnemyRobots.length > 0) {
			buildSomething();
		}
		if(!isExploring && (!updatedLocs || rc.getRoundNum() + rc.getID() % 100 == 0)){
            patienceVal *= 1.5;
            updateLocs();
			updatedLocs = true;
		}
	}

	public void waterLowestHealthTree() throws GameActionException {
		TreeInfo[] treesToWater = nearbyAlliedTrees;
		TreeInfo treeToHeal = Util.leastHealth(treesToWater, true);
		if (treeToHeal != null) {
			rc.water(treeToHeal.getID());
		}
	}

	public void buildSomething() throws GameActionException {
		int typeToBuild = Message.GARDENER_BUILD_ORDERS.getValue();
		int myGenetics = Message.GENETICS.getValue();
		int myAdaptation = Message.ADAPTATION.getValue();
		/*
		if(rc.getTeamBullets() > 500 && tankBuilder && (MapAnalysis.numTank + 1) * 4 < MapAnalysis.numSoldier){
			if(buildRobot(RobotType.TANK, true)){
				return;
			}
		}*/
		if (nearbyEnemyRobots.length == 0  && rc.getRoundNum() > 5 && (rc.readBroadcast(15) == 0 || rc.getRoundNum() < 20 && MapAnalysis.conflictDist > 10) && plantATree())
			return;
		else if (rc.getBuildCooldownTurns() == 0 && (rc.readBroadcast(15) > 0)) {
			switch (typeToBuild) {
			case 0:
				break;
			case 1:
				if (buildRobot(RobotType.SOLDIER, true)) {
					return;
				}
				break;
			case 2:
				/*
				if(tankBuilder && debug)
					System.out.println("trying to build tank");
				if (tankBuilder && buildRobot(RobotType.TANK, true)) {
					return;
				}
				else if(tankBuilder && rc.getTeamBullets() > 300){
					turnsTank++;
				}*/
				break;
			case 3:
				if (buildRobot(RobotType.SCOUT, true)) {
					return;
				}
				break;
			case 4:
				if (buildRobot(RobotType.LUMBERJACK, true)) {
					return;
				}
				break;
			case 5:
				break;
			}
		}
		else if(rc.getBuildCooldownTurns() == 0 && nearbyEnemyRobots.length > 0 && (nearbyEnemyRobots.length != 1 || nearbyEnemyRobots[0].type != RobotType.ARCHON)){
			buildRobot(RobotType.SOLDIER, false);
		}
	}

	public boolean buildRobot(RobotType type, boolean dec) throws GameActionException {
		if (rc.getTeamBullets() < type.bulletCost)
			return false;
		Direction dir = here.directionTo(MapAnalysis.center);
		if (rc.canBuildRobot(type, dir)) {
			rc.buildRobot(type, dir);
			if(dec)
				rc.broadcast(15, rc.readBroadcast(15) - 1);
			switch (type) {
			case SOLDIER:
				Message.NUM_SOLDIERS.setValue(Message.NUM_SOLDIERS.getValue() + 1);
				break;
			case TANK:
				Message.NUM_TANKS.setValue(Message.NUM_TANKS.getValue() + 1);
				break;
			case SCOUT:
				Message.NUM_SCOUTS.setValue(Message.NUM_SCOUTS.getValue() + 1);
				break;
			case LUMBERJACK:
				Message.NUM_LUMBERJACKS.setValue(Message.NUM_LUMBERJACKS.getValue() + 1);
				break;
			default:
				break;
			}
			return true;
		}
		int dirsToCheck = 72;
		Direction left = dir.rotateLeftDegrees(360/dirsToCheck);
		Direction right = dir.rotateRightDegrees(360/dirsToCheck);
		for (int i = dirsToCheck; i-- > 0;) {
			if (rc.canBuildRobot(type, left)) {
				rc.buildRobot(type, left);
				if(dec)
					rc.broadcast(15, rc.readBroadcast(15) - 1);
				switch (type) {

				case SOLDIER:
					Message.NUM_SOLDIERS.setValue(Message.NUM_SOLDIERS.getValue() + 1);
					break;
				case TANK:
					Message.NUM_TANKS.setValue(Message.NUM_TANKS.getValue() + 1);
					break;
				case SCOUT:
					Message.NUM_SCOUTS.setValue(Message.NUM_SCOUTS.getValue() + 1);
					break;
				case LUMBERJACK:
					Message.NUM_LUMBERJACKS.setValue(Message.NUM_LUMBERJACKS.getValue() + 1);
					break;
				default:
					break;
				}
				return true;
			}
			if (rc.canBuildRobot(type, right)) {
				rc.buildRobot(type, right);
				if(dec)
					rc.broadcast(15, rc.readBroadcast(15) - 1);
				switch (type) {

				case SOLDIER:
					Message.NUM_SOLDIERS.setValue(Message.NUM_SOLDIERS.getValue() + 1);
					break;
				case TANK:
					Message.NUM_TANKS.setValue(Message.NUM_TANKS.getValue() + 1);
					break;
				case SCOUT:
					Message.NUM_SCOUTS.setValue(Message.NUM_SCOUTS.getValue() + 1);
					break;
				case LUMBERJACK:
					Message.NUM_LUMBERJACKS.setValue(Message.NUM_LUMBERJACKS.getValue() + 1);
					break;
				default:
					break;
				}
				return true;
			}
			left = left.rotateLeftDegrees(360/dirsToCheck);
			right = right.rotateRightDegrees(360/dirsToCheck);
		}
		return false;
	}

	public boolean plantATree() throws GameActionException {
		Direction dir = here.directionTo(MapAnalysis.center);
		Boolean skipped = false;
		for (int i = 36; i-- > 0;) {
			if (rc.canPlantTree(dir)) {
				if (skipped) {
					rc.plantTree(dir);
					return true;
				} else {
					skipped = true;
					dir = dir.rotateLeftDegrees(60);
					i -= 6;
				}
			}
			dir = dir.rotateLeftDegrees(10);
		}
		return false;
	}

}