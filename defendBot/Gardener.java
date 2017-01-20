package defendBot;

import battlecode.common.*;

public class Gardener extends Bot {
	public boolean isExploring;
	public static Direction dirIAmMoving;
	public Gardener(RobotController r) throws GameActionException {
		super(r);
		isExploring = true;
		// anything else gardener specific
	}

	private static Direction findOpenSpaces() throws GameActionException {

		Direction dir = new Direction(0);
		int thingsInTheWay = 0;
		int bestScore = 10000;
		Direction bestDir = new Direction(0);
		for (int i = 0; i < 16; i++) {
			if (!rc.onTheMap(here.add(dir, (float) (type.sensorRadius - .001)))) {
				thingsInTheWay ++;
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
		if (isExploring) {
			if (dirIAmMoving == null || myRand.nextDouble() < .1 + (double)(-rc.getRoundNum())/(double)(10*rc.getRoundLimit())) {
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
		if ( rc.getRoundNum() > 100 && nearbyEnemyRobots.length == 0 && plantATree() )
			return;
		if (rc.getBuildCooldownTurns() == 0 && (rc.readBroadcast(15) > 0 || nearbyEnemyRobots.length != 0)) {
			buildRobot(RobotType.LUMBERJACK);
		}
	}

	public boolean buildRobot(RobotType type) throws GameActionException {
		if (rc.getTeamBullets() < type.bulletCost)
			return false;
		Direction dir = here.directionTo(MapAnalysis.center);
		for (int i = 36; i-- > 0;) {
			if (rc.canBuildRobot(type, dir)) {
				rc.buildRobot(type, dir);
				rc.broadcast(15, rc.readBroadcast(15) - 1);
				rc.broadcast(9, rc.readBroadcast(9)+1);
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
		for (int i = 36; i-- > 0;) {
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