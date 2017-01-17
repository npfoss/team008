package team008.oldBot2;

import battlecode.common.*;

public class Gardener extends Bot {
	public boolean isExploring;
	public int defendersBuilt;
	public boolean incrementedSignals;

	public Gardener(RobotController r) throws GameActionException {
		super(r);
		isExploring = true;
		incrementedSignals = false;
		// anything else gardener specific
	}

	private static Direction findOpenSpaces() throws GameActionException {

		Direction dir = new Direction(0);
		int thingsInTheWay = 0;
		int bestScore = 10000;
		Direction bestDir = new Direction(0);
		for (int i = 0; i < 16; i++) {
			if (!rc.onTheMap(here.add(dir, (float) (type.sensorRadius - .001)))) {
				thingsInTheWay += 100;
			}
			for (TreeInfo t : nearbyAlliedTrees)
				if (dir.radiansBetween(here.directionTo(t.getLocation())) < Math.PI / 2) {
					thingsInTheWay++;
				}
			for (RobotInfo t : nearbyRobots)
				if ((t.type == RobotType.ARCHON || t.type == RobotType.GARDENER)
						&& dir.radiansBetween(here.directionTo(t.getLocation())) < Math.PI / 2) {
					thingsInTheWay += (t.type == RobotType.ARCHON ? 1 : 10);
				}

			if (thingsInTheWay < bestScore) {
				bestDir = dir;
				bestScore = thingsInTheWay;
			}
			dir = dir.rotateLeftDegrees((float) 22.5);
			thingsInTheWay = 0;
		}

		return bestDir;

	}

	public void takeTurn() throws GameActionException {
		waterLowestHealthTree();
		if (nearbyEnemyRobots.length > 0) {
				Messaging.sendDistressSignal(here);
				//better build orders for defending against rushes
				int numSignals = rc.readBroadcast(21);
				if(numSignals < 4 && !incrementedSignals){//not already at max defense
					numSignals++;
					incrementedSignals = true;
					rc.broadcast(21, numSignals);
					if(numSignals == 5){
						rc.broadcast(12,4);
					}
					else if(numSignals == 1){
						rc.broadcast(12,2);
					}
				}
		}
		if (isExploring) {
			if (dirIAmMoving == null || myRand.nextDouble() < .2) {
				dirIAmMoving = findOpenSpaces();
			}
			int temp = Clock.getBytecodeNum();
			tryMoveDirectionDangerous(dirIAmMoving);
			System.out.println("Used in moving " + (Clock.getBytecodeNum() - temp) );
			boolean farAway = true;
			for (RobotInfo r : nearbyAlliedRobots) {
				if (r.type == RobotType.GARDENER || r.type == RobotType.ARCHON) {
					farAway = false;
					break;
				}
			}
			for (TreeInfo r : nearbyAlliedTrees) {
				if (Util.distanceSquaredTo(r.location, here) < 42.25) { // 6.5^2
					farAway = false;
					break;
				}
			}
			isExploring = !farAway;
			if (rc.getRoundNum() < 10) {
				isExploring = false;
			}
		}
		if (!isExploring) {
			buildSomething();
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
		int distressLevel = rc.readBroadcast(12);
		int mapType = rc.readBroadcast(11);
		int numGardenersBuilt = rc.readBroadcast(22);
		int typeToBuild = rc.readBroadcast(14);
		int numToBuild = rc.readBroadcast(15);
		if (typeToBuild == 3 && numToBuild > 0 && nearbyEnemyRobots.length == 0){
			//System.out.println("I must build Unit Type:" + typeToBuild + ":" + numToBuild);
			if (buildRobot(RobotType.SCOUT)) {
				rc.broadcast(15, numToBuild - 1);
			}
			return;
		}
		boolean iNeedDefenders = (nearbyEnemyRobots.length > 0 || ((defendersBuilt < 1) && (distressLevel > 2 || distressLevel > 1 && roundNum < 300 || mapType == 2 && roundNum < 100 || rc.getTreeCount() == 2 && numGardenersBuilt == 1)));
		if(iNeedDefenders){
			System.out.println("need defenders");
			if (buildRobot(RobotType.LUMBERJACK)) {
				defendersBuilt++;
			}
			return;
		}
		if (plantATree())
			return;
		if (numToBuild > 0) {
			//System.out.println("I must build Unit Type:" + typeToBuild + ":" + numToBuild);
			switch (typeToBuild) {
			case 0:
				break;
			case 1:
				if (buildRobot(RobotType.SOLDIER)) {
					defendersBuilt++;
					rc.broadcast(15, numToBuild - 1);
					return;
				}
				break;
			case 2:
				if (buildRobot(RobotType.TANK)) {
					defendersBuilt++;
					rc.broadcast(15, numToBuild - 1);
					return;
				}
				break;
			case 3:
				if (buildRobot(RobotType.SCOUT)) {
					rc.broadcast(15, numToBuild - 1);
					return;
				}
				break;
			case 4:
				if (buildRobot(RobotType.LUMBERJACK)) {
					defendersBuilt++;
					rc.broadcast(15, numToBuild - 1);
					return;
				}
				break;
			case 5:
				break;
			}
		} else {
			plantATree();
		}
	}

	public boolean buildRobot(RobotType type) throws GameActionException {
		if (rc.getTeamBullets() < type.bulletCost)
			return false;
		Direction dir = here.directionTo(MapAnalysis.center);
		for (int i = 36; i-- > 0;) {
			if (rc.canBuildRobot(type, dir)) {
				rc.buildRobot(type, dir);
				return true;
			} else {
				dir = dir.rotateLeftDegrees(10);
			}
		}
		return false;
	}

	public boolean plantATree() throws GameActionException {

		Direction dir = here.directionTo(MapAnalysis.center);
		Boolean skipped = false;
		for (int i = 35; i-- > 0;) {
			if (rc.canPlantTree(dir)) {
				if (skipped) {
					rc.plantTree(dir);
					return true;
				} else {
					skipped = true;
				}
			}
			if (skipped) {
				dir = dir.rotateLeftDegrees(60);
				i -= 5;
			} else {
				dir = dir.rotateLeftDegrees(10);
			}
		}
		return false;
	}

}