package team008.finalBot;

import battlecode.common.*;

public class Gardener extends Bot {
	public boolean isExploring;
	public static Direction dirIAmMoving;
	public static boolean updatedLocs;

	public Gardener(RobotController r) throws GameActionException {
		super(r);
		isExploring = true;
		updatedLocs = false;
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
		if( here.distanceTo(targetLoc) < type.sensorRadius-.001 && (!rc.onTheMap(targetLoc) || rc.isLocationOccupiedByTree(targetLoc)) || !rc.onTheMap(here.add(here.directionTo(targetLoc),(float) (type.sensorRadius-.1)))){
			Message.GARDENER_BUILD_LOCS.removeLocation(targetLoc);
			return true;
		}
		return false;
	}
	public void updateLocs() throws GameActionException{
		for(int i = 0; i < 6 ; i++)
		Message.GARDENER_BUILD_LOCS.addLocation(here.add(new Direction((float) (Math.PI/3 * i)), (float) 8.5));
	}
	public void takeTurn() throws GameActionException {
		waterLowestHealthTree();
		if (nearbyEnemyRobots.length > 0) {
			//System.out.println("sent target d");
			Message.DISTRESS_SIGNALS.addLocation(nearbyEnemyRobots[0].location);
			if (rc.getRoundNum() < 200) {
				switch (nearbyEnemyRobots[0].type) {
				case SCOUT:
					Message.ADAPTATION.setValue(MapAnalysis.DEFEND_SCOUT);
				case LUMBERJACK:
					Message.ADAPTATION.setValue(MapAnalysis.DEFEND_LUMBERJACK);
				case SOLDIER:
					Message.ADAPTATION.setValue(MapAnalysis.DEFEND_SOLDIER);
				default:
					break;

				}
			}
		}
		if (isExploring) {
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
					if (r.type == RobotType.GARDENER || r.type == RobotType.ARCHON) {
						farAway = false;
						break;
					}
				}
				isExploring = !farAway;
			} else {
				goToDangerous(targetLoc);
				if (here.distanceTo(targetLoc) < .1) {
					isExploring = false;
				}
			}
			if (Message.NUM_GARDENERS.getValue() == 1) {
				isExploring = false;
			}
		}
		if (!isExploring
				|| nearbyEnemyRobots.length > 0 && Message.GARDENER_BUILD_ORDERS.getValue() != MapAnalysis.TANK) {
			buildSomething();
		}
		if(!isExploring && !updatedLocs){
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
		if ((!(myGenetics == MapAnalysis.RUSH_ENEMY) || rc.getRoundNum() > 100) && nearbyEnemyRobots.length == 0  && rc.getRoundNum() > 5 && (typeToBuild != MapAnalysis.TANK || rc.readBroadcast(15) == 0) && plantATree())
			return;
		else if (rc.getBuildCooldownTurns() == 0 && (rc.readBroadcast(15) > 0)) {
			switch (typeToBuild) {
			case 0:
				break;
			case 1:
				if (buildRobot(RobotType.SOLDIER)) {
					return;
				}
				break;
			case 2:
				if (buildRobot(RobotType.TANK)) {
					return;
				}
				break;
			case 3:
				if (buildRobot(RobotType.SCOUT)) {
					return;
				}
				break;
			case 4:
				if (buildRobot(RobotType.LUMBERJACK)) {
					return;
				}
				break;
			case 5:
				break;
			}
		}
	}

	public boolean buildRobot(RobotType type) throws GameActionException {
		if (rc.getTeamBullets() < type.bulletCost)
			return false;
		Direction dir = here.directionTo(MapAnalysis.center);
		if (rc.canBuildRobot(type, dir)) {
			rc.buildRobot(type, dir);
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
		Direction left = dir.rotateLeftDegrees(10);
		Direction right = dir.rotateRightDegrees(10);
		for (int i = 18; i-- > 0;) {
			if (rc.canBuildRobot(type, left)) {
				rc.buildRobot(type, left);
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
			left = left.rotateLeftDegrees(10);
			right = right.rotateRightDegrees(10);
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
					dir = dir.rotateLeftDegrees(50);
					i -= 5;
				}
			}
			dir = dir.rotateLeftDegrees(10);
		}
		return false;
	}

}